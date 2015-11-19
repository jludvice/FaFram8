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
	 * Create pool of nodes prepared to be assigned to containers.
	 *
	 * @param containerList list of containers
	 */
	void createServerPool(List<Container> containerList);

	/**
	 * Assign IP addresses of created nodes to containers. If container is marked as root public IP should be assigned.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	void assignAddresses(List<Container> containerList);

	/**
	 * Release all allocated resources.
	 */
	void releaseResources();
}
