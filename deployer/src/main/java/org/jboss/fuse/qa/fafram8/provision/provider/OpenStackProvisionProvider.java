package org.jboss.fuse.qa.fafram8.provision.provider;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.EmptyContainerListException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exception.InstanceAlreadyExistsException;
import org.jboss.fuse.qa.fafram8.exception.NoIPAddressException;
import org.jboss.fuse.qa.fafram8.exception.OfflineEnvironmentException;
import org.jboss.fuse.qa.fafram8.exception.UniqueServerNameException;
import org.jboss.fuse.qa.fafram8.exceptions.CopyFileException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackClient;
import org.jboss.fuse.qa.fafram8.provision.openstack.ServerInvokerPool;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenStackProvisionProvider class used for calling all OpenStack node operations. Using authenticated OpenStackClient singleton.
 * <p/>
 * Created by ecervena on 24.9.15.
 * TODO(ecervena): this should be probably singleton
 */
@Slf4j
public class OpenStackProvisionProvider implements ProvisionProvider {

	// Server boot timeout
	private static final int BOOT_TIMEOUT = 120000;

	// Server invoker pool instance
	private final ServerInvokerPool invokerPool = new ServerInvokerPool();

	// List of floating addresses allocated by OpenStackProvisionProvider
	private static final List<FloatingIP> floatingIPs = new LinkedList<>();

	// List of all created OpenStack nodes a.k.a. servers
	private static final List<Server> serverRegister = new LinkedList<>();

	// List of available OpenStack nodes which are not assigned to container yet
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private static List<Server> serverPool = new LinkedList<>();

	// Authenticated OpenStackClient instance
	@Getter
	private final OSClient os = OpenStackClient.getInstance();

	private static final String OFFLINE_IPTABLES_FILE = "iptables-no-internet";

	private String ipTablesFilePath;

	/**
	 * This method will use ConfigurationParser singleton to parse XML representation of OpenStack infrastructure
	 * and spawn thread for each container to create its OpenStack node. Each thread will create one OpenStack node,
	 * assign its serverId to container object model and register new server to OpenStackProvisionProvider's ServerList.
	 * IP addresses are assigned to containers later. Root container will get floating public IP and register it.
	 * Others will get only local IP.
	 *
	 * @param containerList list of containers
	 */
	@Override
	public void createServerPool(List<Container> containerList) {
		log.info("Spawning OpenStack infrastructure.");
		invokerPool.spawnServers(containerList);
	}

	/**
	 * Assign IP addresses to list of containers. Container marked as root will get public IP.
	 * Check that all servers for containers are online and SSH server is online.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	@Override
	public void assignAddresses(List<Container> containerList) {
		if (containerList.isEmpty()) {
			throw new EmptyContainerListException("Container list is empty!");
		}
		for (Container container : containerList) {
			final Server server = getServerByName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX)
					+ "-" + container.getName());
			if (container.getNode() == null) {
				// We dont have any info, use the defaults
				container.setNode(
						Node.builder()
								.port(SystemProperty.getHostPort())
								.username(SystemProperty.getHostUser())
								.password(SystemProperty.getHostPassword())
								.build());
			}
			container.getNode().setNodeId(server.getId());

			if (container.isRoot()) {
				final String ip = assignFloatingAddress(server.getId());
				log.info("Assigning public IP: " + ip + " for container: " + container.getName());
				container.getNode().setHost(ip);
				container.getNode().setExecutor(container.getNode().createExecutor());
				removeServerFromPool(server);
			} else {
				//fuseqe-lab has only 1 address type "fuseqe-lab-1" with only one address called NovaAddress
				setLocalIPToContainer(container, server);
				log.info("Assigning local IP: " + server.getAddresses().getAddresses(SystemProperty.getExternalProperty(FaframConstant
						.OPENSTACK_ADDRESS_TYPE)).get(0).getAddr() + " for container: " + container.getName());
				removeServerFromPool(server);
			}
		}

		final Executor executor = createExecutor(containerList);
		try {
			// This will wait for startup of server for root container
			executor.connect();
		} catch (FaframException ex) {
			throw new FaframException("Connection couldn't be established after " + SystemProperty.getStartWaitTime() + " seconds to "
					+ executor.getClient().getHost());
		}

		for (Container container : containerList) {
			// Iterate over all container and try to connect to them (exclude child containers)
			if (!(container instanceof ChildContainer)) {
				canConnect(executor, container);
			}
		}
		//TODO(ecervena): add ip assigment control
	}

	/**
	 * Release allocated OpenStack resources. Method will delete created servers and release allocated floating IPs.
	 */
	@Override
	public void releaseResources() {
		if (SystemProperty.isKeepOsResources()) {
			log.warn("Keeping OpenStack resources. Don't forget to release them later!");
			return;
		}

		log.info("Releasing allocated OpenStack resources.");
		for (int i = floatingIPs.size() - 1; i >= 0; i--) {
			final FloatingIP ip = floatingIPs.get(i);
			log.info("Deallocating floating IP: " + ip.getFloatingIpAddress());
			os.compute().floatingIps().deallocateIP(ip.getId());
			floatingIPs.remove(i);
		}

		for (int i = serverRegister.size() - 1; i >= 0; i--) {
			final Server server = serverRegister.get(i);
			log.info("Terminating server: " + server.getName());
			os.compute().servers().delete(server.getId());
			serverRegister.remove(i);
		}

		log.info("All OpenStack resources has been released successfully");
	}

	@Override
	public void loadIPTables(List<Container> containerList) {
		// "If" for deciding if this method should be used is moved here so the Fafram method is clean(Only for you ecervena <3)
		if (SystemProperty.getIptablesConfFilePath().isEmpty() && !SystemProperty.isOffline()) {
			// There was no iptables configuration file set so the user doesn't want to change environment.
			return;
		}

		log.info("Loading iptables configuration files.");

		final Executor executor = createExecutor(containerList);
		executor.connect();

		setCorrectIpTablesFilePath(executor);

		// If the environment should be configured by custom iptables configuration file from localhost then copy the file to remote root node
		if (!SystemProperty.isOffline()) {
			try {
				log.debug("Copying iptables configuration file on node: " + executor.getClient().toString());
				((NodeSSHClient) executor.getClient()).copyFileToRemote(SystemProperty.getIptablesConfFilePath(), this.ipTablesFilePath);
			} catch (CopyFileException e) {
				throw new FaframException("Problem with copying iptables configuration file to node: " + executor.getClient().getHost() + ".", e);
			}
		}

		// For each container in container list execute and set a correct iptables configuration file
		for (Container c : containerList) {
			if (c instanceof ChildContainer) {
				// If the child container is child then skip. The file will be copied and executed for all ssh containers
				// and root. It doesn't make sense to do also for child containers.
				continue;
			}
			executeIpTables(executor, c);
		}

		log.info("IPTables configuration files successfully loaded on all nodes! Environment configuration according to {} file.", this.ipTablesFilePath);
	}

	@Override
	public void cleanIpTables(List<Container> containerList) {
		// Do nothing
	}

	@Override
	public void checkNodes(List<Container> containerList) {
		for (Container c : containerList) {
			if (getServers(SystemProperty.getOpenstackServerNamePrefix() + "-" + c.getName()).size() != 0) {
				throw new InstanceAlreadyExistsException("Instance " + SystemProperty.getOpenstackServerNamePrefix()
						+ "-" + c.getName() + " already exists!");
			}
		}
	}

	/**
	 * Register server to OpenStackProvisionProvider's "register".
	 *
	 * @param server server
	 */
	public static void registerServer(Server server) {
		serverRegister.add(server);
	}

	/**
	 * Add server to pool.
	 *
	 * @param server representation of openstack node object
	 */
	public static void addServerToPool(Server server) {
		serverPool.add(server);
	}

	/**
	 * Remove server from pool.
	 *
	 * @param server representation of openstack node object
	 */
	public static void removeServerFromPool(Server server) {
		serverPool.remove(server);
	}

	/**
	 * Create new OpenStack node. Method will create Server object model, boot it,
	 * add to register and wait for active status.
	 *
	 * @param serverName name of the new node
	 */
	public void spawnNewServer(String serverName) {
		log.info("Spawning new server: "
				+ SystemProperty.getOpenstackServerNamePrefix()
				+ "-"
				+ serverName);
		final ServerCreate server = os
				.compute()
				.servers()
				.serverBuilder()
				.image(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_IMAGE))
				.name(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX) + "-" + serverName)
				.flavor(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_FLAVOR))
				.keypairName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_KEYPAIR))
				.build();
		final Server node = os.compute().servers().bootAndWaitActive(server, BOOT_TIMEOUT);
		serverRegister.add(node);
	}

	/**
	 * Method for deleting OpenStack node by server name. All nodes created  by OpenStackProvisionProvider have "fafram8-" prefix.
	 *
	 * @param serverName name of the node
	 */
	public void deleteServer(String serverName) {
		os.compute().servers().delete(getServerByName(serverName).getId());
	}

	/**
	 * Method for getting Server a.k.a OpenStack node object model. All nodes created  by OpenStackProvisionProvider
	 * have "fafram8-" prefix.
	 *
	 * @param serverName name of the node
	 * @return Server representation of openstack node object
	 */
	public Server getServerByName(String serverName) {
		final List<Server> equalsList = getServers(serverName);
		if (equalsList.size() != 1) {
			for (Object obj : equalsList) {
				log.error("Server with not unique name detected: ", obj.toString());
			}
			throw new UniqueServerNameException(
					"Server name is not unique. More than 1 (" + equalsList.size() + ") server with specified name: " + serverName + " detected");
		} else {
			return equalsList.get(0);
		}
	}

	/**
	 * Gets the count of the servers with name (prefix + "name"). Used to check if there are already some servers with defined name.
	 *
	 * @param name container name
	 * @return list of servers with given name
	 */
	public List<Server> getServers(String name) {
		final Map<String, String> filter = new HashMap<>();
		if (!name.startsWith(SystemProperty.getOpenstackServerNamePrefix())) {
			name = SystemProperty.getOpenstackServerNamePrefix() + "-" + name;
		}

		filter.put("name", name);

		final List<? extends Server> serverList = os
				.compute()
				.servers()
				.list(filter);
		final List<Server> equalsList = new ArrayList<>();

		for (Server server : serverList) {
			if (name.equals(server.getName())) {
				equalsList.add(server);
			}
		}

		return equalsList;
	}

	/**
	 * Assign floating IP address to specified server.
	 *
	 * @param serverID ID of the server
	 * @return floating IP assigned to server
	 */
	public String assignFloatingAddress(String serverID) {
		final FloatingIP ip = os.compute().floatingIps().allocateIP("public");
		floatingIPs.add(ip);
		final Server server = os.compute().servers().get(serverID);
		os.compute().floatingIps().addFloatingIP(server, ip.getFloatingIpAddress());
		return ip.getFloatingIpAddress();
	}

	/**
	 * Assign local IP address of server to container.
	 *
	 * @param container fafram container object model
	 * @param server openstack node
	 */
	private void setLocalIPToContainer(Container container, Server server) {
		try {
			container.getNode().setHost(server.getAddresses().getAddresses(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_ADDRESS_TYPE))
					.get(0).getAddr());
		} catch (NullPointerException npe) {
			throw new NoIPAddressException("OpenStack server local IP address not found. Maybe server is not active yet.");
		}
	}

	/**
	 * Creates executor to node with root container. Node of the root container has always assigned public IP.
	 *
	 * @param containerList list of containers
	 * @return connected executor
	 */
	private Executor createExecutor(List<Container> containerList) {
		Node rootNode = null;
		for (Container c : containerList) {
			if (c.isRoot()) {
				rootNode = c.getNode();
				break;
			}
		}

		if (rootNode == null) {
			throw new FaframException("There was no root container in container list when loading IP tables!");
		}

		final SSHClient sshClient = new NodeSSHClient().defaultSSHPort().host(rootNode.getHost())
				.username(rootNode.getUsername()).password(rootNode.getPassword());
		final Executor executor = new Executor(sshClient);

		return executor;
	}

	/**
	 * Sets correct path and name to iptables configuration file depending on the type of environment.
	 *
	 * @param executor connected executor to root node
	 */
	private void setCorrectIpTablesFilePath(Executor executor) {
		// This is special case when you want to use default offline configuration.
		if (SystemProperty.isOffline()) {
			// setting path to default iptablec configuration file in home folder of user
			this.ipTablesFilePath = OFFLINE_IPTABLES_FILE;
		} else {
			// Otherwise you want to copy iptables configuration file from local machine to remote node -> create path to file on remote node
			final String directory = ("".equals(SystemProperty.getWorkingDirectory()))
					? executor.executeCommand("pwd") : SystemProperty.getWorkingDirectory();

			// Path to copied iptables file on remote nodes
			this.ipTablesFilePath =
					directory + File.separator + StringUtils.substringAfterLast(SystemProperty.getIptablesConfFilePath(), File.separator);
		}
		log.info("Setting iptables configuration in environment to {}.", this.ipTablesFilePath);
	}

	/**
	 * Executes command line commands on node to successfully load and configure iptables on provided container. If the
	 * custom iptables configuration file is used then this methods also copies cofniguration file to specified node of
	 * container.
	 *
	 * @param executor connected executor to root node
	 * @param container container on which the iptables should be configured
	 */
	private void executeIpTables(Executor executor, Container container) {
		String preCommand = "";

		try {
			if (!container.isRoot()) {
				// This is needed for executing commands on nodes without publicip on openstack.
				// For this purpose we create this string to simulate to connecting to node from root container to which
				// we are connected via the SSHClient.
				// If the container is root this string is empty and the commands are executed only on root.
				preCommand = "ssh -o StrictHostKeyChecking=no " + container.getNode().getUsername() + "@" + container.getNode().getHost() + " ";
			}

			// if offline environment then skip this. The iptables configuration should be present in the image itself.
			if (!SystemProperty.isOffline()) {
				// Copy iptables file already on root node to other nodes via the scp command (hack for nodes without public ip)
				executor.executeCommand("scp -o StrictHostKeyChecking=no " + this.ipTablesFilePath + " " + container.getNode().getHost() + ":"
						+ this.ipTablesFilePath);
			}

			log.debug("Executing iptables configuration file on node: " + executor.toString());

			final String response = executor.executeCommand(preCommand + "sudo cat " + this.ipTablesFilePath);

			if (response.contains("No such file or directory")) {
				throw new OfflineEnvironmentException("Configuration file for iptables"
						+ " doesn't exists on node: " + container.getNode().getHost() + ".",
						new FileNotFoundException("File " + this.ipTablesFilePath + " doesn't exists."));
			}
			executor.executeCommand(preCommand + "sudo iptables-restore " + this.ipTablesFilePath);
			log.debug("Iptables successfully configured on node {}.", executor);
		} catch (Exception e) {
			throw new FaframException("There was problem setting iptables on node: "
					+ container.getNode().getHost(), e);
		}
	}

	/**
	 * Tries to connect specified container's server and check if SSH server is online. Connection is tried through
	 * root container's machine because servers that are intended for SSH containers don't have public IP addresses.
	 * This complicates using SSH client for connecting to these machines.
	 *
	 * @param executor executor for machine that will contain root container
	 * @param container that is tested for running SSH
	 */
	private void canConnect(Executor executor, Container container) {
		log.debug("Testing connection to: " + container.getNode().getHost());
		Boolean connected = false;
		final int step = 10;
		int elapsed = 0;
		final long timeout = step * 1000L;

		log.debug("Waiting for SSH connection ...");
		final String preCommand = "ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 " + container.getNode().getUsername() + "@" + container.getNode().getHost() + " ";
		while (!connected) {
			// Check if the time is up
			if (elapsed > SystemProperty.getProvisionWaitTime()) {
				log.error("Connection couldn't be established after " + SystemProperty.getProvisionWaitTime() + " seconds to container with name \""
						+ container.getName() + "\" with IP " + container.getNode().getHost());
				throw new FaframException("Connection couldn't be established after " + SystemProperty.getProvisionWaitTime() + " seconds to container with name \""
						+ container.getName() + "\" with IP " + container.getNode().getHost());
			}

			String response = executor.executeCommand(preCommand + "echo Connected");
			if ("Connected".equals(response)) {
				response = executor.executeCommand("echo $?");
				if ("0".equals(response)) {
					connected = true;
					log.trace("Connected to remote SSH server {}", container.getNode().getHost());
					continue;
				}
			}
			log.debug("Remaining time: " + (SystemProperty.getProvisionWaitTime() - elapsed) + " seconds. ");
			elapsed += step;
			sleep(timeout);
		}
	}

	/**
	 * Sleeps for given amount of time.
	 *
	 * @param time time in millis
	 */
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
