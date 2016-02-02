package org.jboss.fuse.qa.fafram8.provision.provider;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.exception.EmptyContainerListException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
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

	//server boot timeout
	private static final int BOOT_TIMEOUT = 120000;

	//Server invoker pool instance
	private final ServerInvokerPool invokerPool = new ServerInvokerPool();

	//List of floating addresses allocated by OpenStackProvisionProvider
	private static final List<FloatingIP> floatingIPs = new LinkedList<>();

	//List of all created OpenStack nodes a.k.a. servers
	private static final List<Server> serverRegister = new LinkedList<>();

	//List of available OpenStack nodes which are not assigned to container yet
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private static List<Server> serverPool = new LinkedList<>();

	//Authenticated OpenStackClient instance
	@Getter
	private final OSClient os = OpenStackClient.getInstance();

	private static final String OFFLINE_IPTABLES_FILE = "iptables-no-internet";

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
	//TODO(ecervena): Make getExternalProperty reading SystemProperty first. issue #49
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
		serverRegister.add(os.compute().servers().bootAndWaitActive(server, BOOT_TIMEOUT));
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
	//TODO(ecervena): resolve BUG
	public Server getServerByName(String serverName) {
		final Map<String, String> filter = new HashMap<>();
		filter.put("name", serverName);
		final List<? extends Server> serverList = os
				.compute()
				.servers()
				.list(filter);
		//BUG: os.list(filter) is implemented with .contains(paramValue) insted of .equals(paramValue)
		//So when filtering name field e.g. "fafram-ecervena-1" all servers "fafram-ecervena-1*" is returned
		//Therefor iteration and equality check is needed.
		/*List<Server> equalsList =  new LinkedList<>();
		for (Server server: serverList) {
			System.out.println(serverName);
			System.out.println(server.getName());
			if (serverName.equals(server.getName())) {
				equalsList.add(server);
			}
		}*/
		if (serverList.size() != 1) {
			for (Object obj : serverList) {
				log.error("Server with not unique name detected: ", obj.toString());
			}
			throw new UniqueServerNameException(
					"Server name is not unique. More than 1 (" + serverList.size() + ") server with specified name: " + serverName + " detected");
		} else {
			return serverList.get(0);
		}
	}

	/**
	 * This method will use ConfigurationParser singleton to parse XML representation of OpenStack infrastructure
	 * and spawn thread for each container to create its OpenStack node. Each thread will create one OpenStack node,
	 * assign its serverId to container object model and register new server to OpenStackProvisionProvider's ServerList.
	 * IP addresses are assigned to containers later. Root container will get floating public IP and register it.
	 * Others will get only local IP.
	 *
	 * @param containerList list of containers
	 */
	public void createServerPool(List<Container> containerList) {
		log.info("Spawning OpenStack infrastructure.");
		invokerPool.spawnServers(containerList);
	}

	/**
	 * Assign IP addresses to list of containers. Container marked as root will get public IP.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	public void assignAddresses(List<Container> containerList) {
		if (containerList.isEmpty()) {
			throw new EmptyContainerListException("Container list is empty!");
		}
		for (Container container : containerList) {
			final Server server = getServerByName(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX)
					+ "-" + container.getName());
			container.getNode().setNodeId(server.getId());

			if (container.isRoot()) {
				final String ip = assignFloatingAddress(server.getId());
				log.debug("Assigning public IP: " + ip + " for container: " + container.getName());
				container.getNode().setHost(ip);
				System.setProperty(FaframConstant.HOST, ip);
				removeServerFromPool(server);
			} else {
				//fuseqe-lab has only 1 address type "fuseqe-lab-1" with only one address called NovaAddress
				setLocalIPToContainer(container, server);
				log.debug("Assigning local IP: " + server.getAddresses().getAddresses(SystemProperty.getExternalProperty(FaframConstant
						.OPENSTACK_ADDRESS_TYPE)).get(0).getAddr() + " for container: " + container.getName());
				removeServerFromPool(server);
			}
		}

		//TODO(ecervena): add ip assigment control
	}

	/**
	 * Release allocated OpenStack resources. Method will delete created servers and release allocated floating IPs.
	 */
	public void releaseResources() {
		if (SystemProperty.isKeepOsResources()) {
			log.info("Keeping OpenStack resources. Don't forget to release them later!");
			return;
		}
		log.info("Releasing allocated OpenStack resources.");
		for (FloatingIP ip : floatingIPs) {
			log.info("Deallocating floating IP: " + ip.getFloatingIpAddress());
			os.compute().floatingIps().deallocateIP(ip.getId());
		}
		for (Server server : serverRegister) {
			log.info("Terminating server: " + server.getName());
			os.compute().servers().delete(server.getId());
		}
		log.info("All OpenStack resources has been released successfully");
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

	// TODO(rjakubco): refactor loadIptables and offline method
	@Override
	public void loadIPTables(List<Container> containerList) {
		// This is special case when you want to use default offline configuration.
		if (SystemProperty.isOffline()) {
			offline(containerList);
			return;
		}
		// "If" for deciding if this method should be used is moved here so the Fafram method is clean(Only for you ecervena <3)
		if (SystemProperty.getIptablesConfFilePath().isEmpty()) {
			// There was no iptables configuration file set so the user doesn't want to change environment.
			return;
		}

		log.info("Loading iptables configuration files.");
		SSHClient sshClient;
		Executor executor;

		// TODO(rjakubco): this in case that root container is always first in the list -> it not very nice
		final Node rootNode = containerList.get(0).getNode();

		for (Container c : containerList) {
			if (c instanceof ChildContainer) {
				// If the child container is child then skip. The file will be copied and executed for all ssh containers
				// and root. It doesn't make sense to do also for child containers.
				continue;
			}

			String preCommand = "";
			sshClient = new NodeSSHClient().defaultSSHPort().hostname(rootNode.getHost())
					.username(rootNode.getUsername()).password(rootNode.getPassword());
			executor = new Executor(sshClient);

			try {
				executor.connect();
				final String directory = ("".equals(SystemProperty.getWorkingDirectory()))
						? executor.executeCommand("pwd") : SystemProperty.getWorkingDirectory();

				// Path to copied iptables file on remote nodes
				final String remoteFilePath =
						directory + File.separator + StringUtils.substringAfterLast(SystemProperty.getIptablesConfFilePath(), File.separator);

				log.debug("Copying iptables configuration file on node: " + sshClient.toString());

				if (c.isRoot()) {
					try {
						// Copy file from localhost to root node. The root node is always the fist entry in containerList.
						// This means this copying will be executed before other nodes.
						((NodeSSHClient) sshClient).copyFileToRemote(SystemProperty.getIptablesConfFilePath(), remoteFilePath);
					} catch (CopyFileException e) {
						throw new FaframException("Problem with copying iptables configuration file to node: " + c.getNode().getHost() + ".", e);
					}
				} else {
					// This is needed for executing commands on nodes without publicip on openstack.
					// For this purpose we create this string to simulate to connecting to node from root container to which
					// we are connected via the SSHClient.
					// If the container is root this string is empty and the commands are executed only on root.
					// TODO(rjakubco): maybe do it in nicer way?
					preCommand = "ssh -o StrictHostKeyChecking=no " + c.getNode().getUsername() + "@" + c.getNode().getHost() + " ";

					// Copy iptables file already on root node to other nodes via the scp command (hack for nodes without public ip)
					executor.executeCommand("scp -o StrictHostKeyChecking=no " + remoteFilePath + " " + c.getNode().getHost() + ":"
							+ remoteFilePath);
				}

				log.debug("Executing iptables configuration file on node: " + executor.toString());

				final String response = executor.executeCommand(preCommand + "sudo cat " + remoteFilePath);

				if (response.contains("No such file or directory")) {
					throw new OfflineEnvironmentException("Configuration file for iptables"
							+ " doesn't exists on node: " + c.getNode().getHost() + ".",
							new FileNotFoundException("File " + remoteFilePath + " doesn't exists."));
				}
				executor.executeCommand(preCommand + "sudo iptables-restore " + remoteFilePath);
			} catch (Exception e) {
				throw new FaframException("There was problem setting iptables on node: "
						+ c.getNode().getHost(), e);
			}
		}
	}

	/**
	 * Turning off the internet using the default iptables configuration file that exists in ecervena snapshots on
	 * all nodes specified in provided list. The configuration file used for turning off the internet
	 * is named "iptables-no-internet".
	 * <p/>
	 * The methods also checks that internet is not available on all nodes meaning that that the environment is truly
	 * offline. If there is problem with setting the environment to offline correctly then the runtime exception is thrown.
	 *
	 * @param containerList list of containers
	 */
	private void offline(List<Container> containerList) {
		log.info("Turning off internet in the environment.");
		SSHClient sshClient;
		Executor executor;

		// TODO(rjakubco): this in case that root container is always first in the list -> it not very nice
		final Node rootNode = containerList.get(0).getNode();

		for (Container c : containerList) {
			if (c instanceof ChildContainer) {
				// If the child container is child then skip. The file will be copied and executed for all ssh containers
				// and root. It doesn't make sense to do also for child containers.
				continue;
			}

			String preCommand = "";
			if (!c.isRoot()) {
				// This is needed for executing commands on nodes without publicip on openstack.
				// For this purpose we create this string to simulate to connecting to node from root container to which
				// we are connected via the SSHClient. If the container is root this string is empty and the commands are
				// executed only on root.
				// TODO(rjakubco): maybe do it in nicer way?
				preCommand = "ssh -o StrictHostKeyChecking=no " + c.getNode().getUsername() + "@" + c.getNode().getHost() + " ";
			}

			sshClient = new NodeSSHClient().defaultSSHPort().hostname(rootNode.getHost())
					.username(rootNode.getUsername()).password(rootNode.getPassword());
			executor = new Executor(sshClient);

			log.debug("Turning off internet on node: " + executor.toString());

			try {
				executor.connect();
				String response = executor.executeCommand(preCommand + "sudo cat " + OFFLINE_IPTABLES_FILE);

				if (response.contains("No such file or directory")) {
					throw new OfflineEnvironmentException("Configuration file for iptables"
							+ " doesn't exists on node: " + c.getNode().getHost() + ".",
							new FileNotFoundException("File " + OFFLINE_IPTABLES_FILE + " doesn't exists."));
				}

				executor.executeCommand(preCommand + "sudo iptables-restore " + OFFLINE_IPTABLES_FILE);

				response = executor.executeCommand(preCommand + "curl www.google.com");
				if (!response.contains("Failed to connect") || !response.contains("Network is unreachable")) {
					throw new OfflineEnvironmentException("Internet connection wasn't turn off successfully on node: "
							+ c.getNode().getHost() + ". Check " + OFFLINE_IPTABLES_FILE
							+ " file on the node.");
				}
			} catch (Exception e) {
				throw new OfflineEnvironmentException("There was problem with turning off the internet on node: "
						+ c.getNode().getHost(), e);
			}
		}
	}
}
