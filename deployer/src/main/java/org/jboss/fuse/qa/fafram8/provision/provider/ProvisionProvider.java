package org.jboss.fuse.qa.fafram8.provision.provider;

import org.jboss.fuse.qa.fafram8.cluster.Container;

import java.util.List;

/**
 * ProvisionProvider interface.
 * <p/>
 * Created by ecervena on 7.10.15.
 */
public interface ProvisionProvider {

	/**
	 * Creates pool of nodes prepared to be assigned to containers.
	 *
	 * @param containerList list of containers
	 */
	void createServerPool(List<Container> containerList);

	/**
	 * Assigns IP addresses of created nodes to containers. If container is marked as root public IP should be assigned.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	void assignAddresses(List<Container> containerList);

	/**
	 * Release all allocated resources.
	 */
	void releaseResources();

	/**
	 * Loads iptables configuration file on all nodes specified in containerlist.
	 *
	 * By default the method looks into user's home folder on each node and looks for file specified in fafram property
	 * iptables.conf.file.path.
	 *
	 *
	 * @param containerList list of containers
	 */
	void loadIPtables(List<Container> containerList);

	/**
	 * Mounts all external disks using the "mount -a" command on root node.
	 *
	 * @param containerList list of containers
	 */
	void mountStorageOnRootNode(List<Container> containerList);
}
