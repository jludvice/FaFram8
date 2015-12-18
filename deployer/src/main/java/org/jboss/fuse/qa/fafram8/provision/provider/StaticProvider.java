package org.jboss.fuse.qa.fafram8.provision.provider;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Default provision provider implementation. This implemenation does nothing. Used in case of pre-configured
 * existing hosts hosts.
 * <p>
 * Created by ecervena on 13.10.15.
 */
@Slf4j
public class StaticProvider implements ProvisionProvider {
	/**
	 * Does nothing.
	 *
	 * @param containerList list of containers
	 */
	@Override
	public void createServerPool(List<Container> containerList) {
		log.info("Server instances specified in configuration.");
		if (SystemProperty.getClean()) {
			log.info("Cleaning resources");
			for (Container c : containerList) {
				//TODO(mmelko): Finish containers cleaning.
			}
		}
	}

	/**
	 * Does nothing.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	@Override
	public void assignAddresses(List<Container> containerList) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void releaseResources() {
	}
}
