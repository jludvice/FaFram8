package org.jboss.fuse.qa.fafram8.provision.provider;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.EmptyContainerListException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exception.InstanceAlreadyExistsException;
import org.jboss.fuse.qa.fafram8.exception.OfflineEnvironmentException;
import org.jboss.fuse.qa.fafram8.exception.UniqueServerNameException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackClient;
import org.jboss.fuse.qa.fafram8.provision.openstack.ServerInvokerPool;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
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
			final Server server =
					getServerByName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX) + "-" + container.getName());
			if (container.getNode() == null) {
				// We dont have any info, use the defaults
				container.setNode(Node.builder().port(SystemProperty.getHostPort()).username(SystemProperty.getHostUser())
						.password(SystemProperty.getHostPassword()).build());
			}
			container.getNode().setNodeId(server.getId());

			final String ip = assignFloatingAddress(server.getId());
			log.info("Assigning public IP: " + ip + " for container: " + container.getName());
			container.getNode().setHost(ip);
			container.getNode().setExecutor(container.getNode().createExecutor());
			removeServerFromPool(server);
		}

		for (Container container : containerList) {
			// Iterate over all container and try to connect to them (exclude child containers)
			if (!(container instanceof ChildContainer)) {
				container.getNode().getExecutor().connect();
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

		// For each container in container list execute and set a correct iptables configuration file
		for (Container c : containerList) {
			if (c instanceof ChildContainer) {
				// If the child container is child then skip. The file will be copied and executed for all ssh containers
				// and root. It doesn't make sense to do also for child containers.
				continue;
			}
			executeIpTables(c);
		}

		log.info("IPTables configuration files successfully loaded on all nodes! Environment configuration according to {} file.",
				this.ipTablesFilePath);
	}

	@Override
	public void cleanIpTables(List<Container> containerList) {
		// Do nothing
	}

	@Override
	public void checkNodes(List<Container> containerList) {
		for (Container c : containerList) {
			if (getServers(SystemProperty.getOpenstackServerNamePrefix() + "-" + c.getName()).size() != 0) {
				throw new InstanceAlreadyExistsException(
						"Instance " + SystemProperty.getOpenstackServerNamePrefix() + "-" + c.getName() + " already exists!");
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
		spawnNewServer(serverName, SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_IMAGE));
	}

	/**
	 * Create new OpenStack node. Method will create Server object model, boot it,
	 * add to register and wait for active status.
	 *
	 * @param serverName name of the new node
	 * @param imageID ID of image to spawn
	 */
	public void spawnNewServer(String serverName, String imageID) {
		log.info("Spawning new server: " + SystemProperty.getOpenstackServerNamePrefix() + "-" + serverName);
		final ServerCreate server = os.compute().servers().serverBuilder().image(imageID)
				.name(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX) + "-" + serverName)
				.flavor(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_FLAVOR))
				.keypairName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_KEYPAIR))
				.networks(Arrays.asList(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NETWORKS).split(","))).build();
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

		final List<? extends Server> serverList = os.compute().servers().list(filter);
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
		final FloatingIP ip = os.compute().floatingIps().allocateIP(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_FLOATING_IP_POOL));
		floatingIPs.add(ip);
		final Server server = os.compute().servers().get(serverID);
		os.compute().floatingIps().addFloatingIP(server, ip.getFloatingIpAddress());
		return ip.getFloatingIpAddress();
	}

	/**
	 * Sets correct path and name to iptables configuration file depending on the type of environment.
	 *
	 * @param executor connected executor to root node
	 */
	private void setCorrectIpTablesFilePath(Executor executor) {
		// This is special case when you want to use default offline configuration.
		if (SystemProperty.isOffline()) {
			// setting path to default iptables configuration file in home folder of user
			this.ipTablesFilePath = OFFLINE_IPTABLES_FILE;
		} else {
			// Otherwise you want to copy iptables configuration file from local machine to remote node -> create path to file on remote node
			final String directory =
					("".equals(SystemProperty.getWorkingDirectory())) ? executor.executeCommand("pwd") : SystemProperty.getWorkingDirectory();

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
	 * @param container container on which the iptables should be configured
	 */
	private void executeIpTables(Container container) {
		container.getNode().getExecutor().connect();
		setCorrectIpTablesFilePath(container.getNode().getExecutor());
		try {
			// if offline environment then skip this. The iptables configuration should be present in the image itself.
			if (!SystemProperty.isOffline()) {
				((NodeSSHClient) container.getNode().getExecutor().getClient()).copyFileToRemote(SystemProperty.getIptablesConfFilePath(), this
						.ipTablesFilePath);
			}

			log.debug("Executing iptables configuration file on node: " + container.getNode().getExecutor().toString());

			final String response = container.getNode().getExecutor().executeCommandSilently("stat " + this.ipTablesFilePath);

			if (response == null || response.isEmpty()) {
				throw new OfflineEnvironmentException(
						"Configuration file for iptables" + " doesn't exists on node: " + container.getNode().getHost() + ".",
						new FileNotFoundException("File " + this.ipTablesFilePath + " doesn't exists."));
			}
			container.getNode().getExecutor().executeCommand("sudo iptables-restore " + this.ipTablesFilePath);
			log.debug("Iptables successfully configured on node {}.", container.getNode().getExecutor());
		} catch (Exception e) {
			throw new FaframException("There was problem setting iptables on node: " + container.getNode().getHost(), e);
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
