package org.jboss.fuse.qa.fafram8.provision.provider;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exception.EmptyContainerListException;
import org.jboss.fuse.qa.fafram8.exception.NoIPAddressException;
import org.jboss.fuse.qa.fafram8.exception.UniqueServerNameException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackClient;
import org.jboss.fuse.qa.fafram8.provision.openstack.ServerInvokerPool;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.FloatingIP;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;

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
	 * Create new OpenStack node. Method will create Server object model, boot it and wait for active status.
	 *
	 * @param serverName name of the new node
	 */
	public void spawnNewServer(String serverName) {
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
	public Server getServerByName(String serverName) {
		final Map<String, String> filter = new HashMap<>();
		filter.put("name", serverName);
		final List<? extends Server> serverList = os
				.compute()
				.servers()
				.list(filter);
		if (serverList.size() != 1) {
			throw new UniqueServerNameException("Server name is not unique. More than 1 (" + serverList.size() + ") server with specified name: " + serverName + " detected");
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
			container.getHostNode().setNodeId(server.getId());

			if (container.isRoot()) {
				final String ip = assignFloatingAddress(server.getId());
				log.debug("Assigning public IP: " + ip + " for container: " + container.getName());
				container.getHostNode().setHost(ip);
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
			container.getHostNode().setHost(server.getAddresses().getAddresses(SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_ADDRESS_TYPE))
					.get(0).getAddr());
		} catch (NullPointerException npe) {
			throw new NoIPAddressException("OpenStack server local IP address not found. Maybe server is not active yet.");
		}
	}
}
