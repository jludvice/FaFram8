package org.jboss.fuse.qa.fafram8.deployer;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.HashSet;
import java.util.Set;

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
			log.info("Creating container " + c);
			c.create();
		}
	}

	/**
	 * Destroys all containers from container list.
	 *
	 * @param force flag if the exceptions should be ignored
	 */
	public static void destroy(boolean force) {
		// Do nothing
		if (SystemProperty.isKeepContainers()) {
			return;
		}
		// Temp set which holds all root containers after the first cycle, so that we don't need to mess with concurent mods
		final Set<Container> set = new HashSet<>();
		set.addAll(ContainerManager.getContainerList());

		for (int i = 0; i < ContainerManager.getContainerList().size(); i++) {
			final Container c = ContainerManager.getContainerList().get(i);
			try {
				if (!(c instanceof RootContainer)) {
					c.destroy();
					set.remove(c);
				}
			} catch (Exception ex) {
				if (!force) {
					throw new FaframException("Error while destroying child/ssh container! " + ex);
				}
			}
		}

		for (Container c : set) {
			try {
				c.destroy();
			} catch (Exception ex) {
				if (!force) {
					throw new FaframException("Error while destroying root container! " + ex);
				}
			}
		}
	}
}
