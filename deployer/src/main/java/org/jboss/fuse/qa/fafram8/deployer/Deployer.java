package org.jboss.fuse.qa.fafram8.deployer;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Deployer class. Iterates through the container list and creates all the containers. If there is no container in the container list,
 * it builds a default one from system properties.
 * Created by avano on 19.8.15.
 */
@Slf4j
public final class Deployer {
	/**
	 * Private constructor.
	 */
	private Deployer() {
	}

	/**
	 * Creates all containers from the container list. If the container list is empty, it adds the default root built from system properties.
	 */
	public static void deploy() {
		if (ContainerManager.getContainerList().isEmpty()) {
			final Container c = RootContainer.builder().defaultRoot().build();
			log.info("Creating default root container");
			log.debug("" + c);
			ContainerManager.getContainerList().add(c);
		}

		for (Container c : ContainerManager.getContainerList()) {
			c.create();
		}
	}

	/**
	 * Destroys all containers from container list.
	 *
	 * @param force flag if the exceptions should be ignored
	 */
	public static void destroy(boolean force) {
		// First shut down all childs and sshs
		List<Container> list = ContainerManager.getContainerList();
		for (int i = list.size() - 1; i >= 0; i--) {
			try {
				if (!(list.get(0) instanceof RootContainer)) {
					list.get(0).destroy();
					list.remove(i);
				}
			} catch (Exception ex) {
				if (!force) {
					throw new FaframException("Error while destroying child/ssh container!" + ex);
				} else {
					log.warn("Error while destroying child/ssh container!" + ex);
				}
			}
		}

		list = ContainerManager.getContainerList();
		for (int i = list.size() - 1; i >= 0; i--) {
			try {
				list.get(0).destroy();
				list.remove(i);
			} catch (Exception ex) {
				if (!force) {
					throw new FaframException("Error while destroying root container!" + ex);
				} else {
					log.warn("Error while destroying root container!" + ex);
				}
			}
		}
	}
}
