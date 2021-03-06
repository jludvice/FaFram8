package org.jboss.fuse.qa.fafram8.deployer;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.JoinContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exception.FaframThreadException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.openstack.exception.InvokerPoolInterruptedException;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.util.Option;
import org.jboss.fuse.qa.fafram8.util.OptionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Deployer class. Iterates through the container list and creates all the containers. If there is no container in the container list,
 * it builds a default one from system properties.
 * Created by avano on 19.8.15.
 */
@Slf4j
public final class Deployer {
	private static final int TIMEOUT = 3;
	private static final int THREAD_POOL = 10;

	@Getter
	@Setter
	private static volatile boolean fail = false;

	@Getter
	private static ConcurrentHashMap<String, ContainerSummoner> summoningThreads = new ConcurrentHashMap<>();
	@Getter
	private static ConcurrentHashMap<Container, ContainerAnnihilator> annihilatingThreads = new ConcurrentHashMap<>();

	/**
	 * Private constructor.
	 */
	private Deployer() {
	}

	/**
	 * Creates all containers from the container list.
	 */
	public static void deploy() {
		// Convert all parentName attributes to parent container object on all containers
		for (Container container : ContainerManager.getContainerList()) {
			setNodeIfNecessary(container);
			if (!(container instanceof RootContainer)) {
				if (container.getParent() == null) {
					// Search the parent by its name
					final Container parent = ContainerManager.getContainer(container.getParentName());
					if (parent == null) {
						throw new FaframException(String.format("Specified parent (%s) of container %s does not exist in container list!",
								container.getParentName(), container.getName()));
					}
					container.setParent(parent);
				}
			}
		}

		if (SystemProperty.isWithThreads()) {
			log.info("*******************************Deploying with THREADS*******************************");
			deployWithThreads();
			ContainerManager.createEnsemble();
		} else {
			// Multithread deployment on windows is not supported...unfortunately....
			checkOSandConvertContainers();
			for (Container c : ContainerManager.getContainerList()) {
				if (!c.isCreated()) {
					c.create();
				}
				if (ContainerManager.isEnsembleReady() && !ContainerManager.isEnsembleCreated()) {
					ContainerManager.createEnsemble();
				}
			}
		}
	}

	/**
	 * Sets the node to the container if the container has "setNodeAs" set.
	 *
	 * @param c container
	 */
	private static void setNodeIfNecessary(Container c) {
		if (!OptionUtils.getString(c.getOptions(), Option.SAME_NODE_AS).isEmpty()) {
			c.setNode(Node.builder(ContainerManager.getContainer(OptionUtils.getString(c.getOptions(), Option.SAME_NODE_AS)).getNode()).build());
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

		if (ContainerManager.isEnsembleCreated()) {
			log.info("Emsemble was created, removing containers from ensemble before destroying");
			ContainerManager.destroyEnsemble();
		}

		// Doing thread cleaning is not the best idea because it is not really stable.
		// For now just do it in the stable old way
		destroyWithoutThreads(force);
	}

	/**
	 * Creates containers using threads.
	 */
	private static void deployWithThreads() {
		final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL);
		final Set<Future> futureSet = new HashSet<>();

		for (Container c : ContainerManager.getContainerList()) {
			final ContainerSummoner containerSummoner;
			if (!c.isCreated()) {
				setNodeIfNecessary(c);
				if (c instanceof RootContainer && !(c instanceof JoinContainer)) {
					containerSummoner = new ContainerSummoner(c, null);
				} else {
					final ContainerSummoner parentSummoner = summoningThreads.get(c.getParent().getName());
					OptionUtils.getString(c.getOptions(), Option.SAME_NODE_AS);
					containerSummoner = new ContainerSummoner(c, parentSummoner);
				}
				summoningThreads.putIfAbsent(c.getName(), containerSummoner);
				log.debug("Creating thread for spawning container: " + c.getName());
				futureSet.add(executorService.submit(containerSummoner));
			}
		}

		Exception storedException = null;
		boolean failed = false;
		for (Future future : futureSet) {
			try {
				future.get();
			} catch (Exception e) {
				log.trace("Exception thrown from the thread ", e);
				failed = true;
				storedException = e;
				break;
			}
		}

		if (failed || ContainerSummoner.isStopWork()) {
			ContainerSummoner.setStopWork(true);
			for (Future future : futureSet) {
				future.cancel(true);
			}
			executorService.shutdownNow();

			throw new FaframThreadException("Deployment failed: " + storedException, storedException);
		}

		executorService.shutdown();
		log.trace("Waiting for ContainerSummoner threads to finish a job.");
		try {
			while (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
				log.trace("Waiting for ContainerSummoner threads to finish a job.");
			}
		} catch (InterruptedException ie) {
			throw new InvokerPoolInterruptedException(ie.getMessage());
		}
	}

	/**
	 * NOT USED
	 * Destroys containers using threads.
	 *
	 * @param force flag if the exceptions should be ignored
	 * @deprecated Not used at the moment for it is not stable enough
	 */
	@Deprecated
	private static void destroyWithThreads(boolean force) {
		final ExecutorService executorService = Executors.newFixedThreadPool(10);
		final Set<Future> futureSet = new HashSet<>();

		// Map containing all created annihilators with the name of theirs container

		final List<Container> list = ContainerManager.getContainerList();
		for (int i = list.size() - 1; i >= 0; i--) {
			final Container c = list.get(i);
			final ContainerAnnihilator containerAnnihilator;
			if (c.isCreated()) {
				if (c instanceof RootContainer || c instanceof SshContainer) {
					// children
					final Set<ContainerAnnihilator> children = new HashSet<>();
					// Find threads of child containers
					for (Container child : ContainerManager.getChildContainers(c)) {
						children.add(annihilatingThreads.get(child));
					}

					containerAnnihilator = new ContainerAnnihilator(c, children, force);
				} else {
					// Annihilator for child container
					containerAnnihilator = new ContainerAnnihilator(c, null, force);
				}
				annihilatingThreads.putIfAbsent(c, containerAnnihilator);
				log.debug("Creating thread for deleting container: " + c.getName());
				futureSet.add(executorService.submit(containerAnnihilator));
			}
		}

		Exception storedException = null;
		boolean flag = false;
		for (Future future : futureSet) {
			try {
				future.get();
			} catch (Exception e) {
				log.error("Exception thrown from the thread ", e);
				flag = true;
				storedException = e;
				break;
			}
		}

		if (flag || ContainerAnnihilator.isStopWork()) {
			// TODO(rjakubco): Create better error message
			log.error("Shutting down annihilating threads because flag " + flag + " or " + ContainerAnnihilator.isStopWork());
			ContainerAnnihilator.setStopWork(true);
			for (Future future : futureSet) {
				future.cancel(true);
			}
			executorService.shutdownNow();

			if (!force) {
				throw new FaframException("Deployment failed: ", storedException);
			}
		}

		executorService.shutdown();
		log.trace("Waiting for ContainerAnnihilator threads to finish a job.");
		try {
			while (!executorService.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
				log.trace("Waiting for ContainerAnnihilator threads to finish a job.");
			}
		} catch (InterruptedException ie) {
			ContainerAnnihilator.setStopWork(true);
			if (!force) {
				throw new InvokerPoolInterruptedException(ie.getMessage());
			}
		}
	}

	/**
	 * Destroys using only one thread.
	 *
	 * @param force flag if the exceptions should be ignored
	 */
	private static void destroyWithoutThreads(boolean force) {
		for (int i = ContainerManager.getContainerList().size() - 1; i >= 0; i--) {
			final Container c = ContainerManager.getContainerList().get(i);

			try {
				c.destroy();
			} catch (Exception ex) {
				ex.printStackTrace();
				if (!force) {
					throw new FaframException("Error while destroying container! " + ex);
				}
			}
		}
	}

	/**
	 * Connects to each node of SSH containers and check if OS is windows. If it is then convert SSHContainer to JoinContainer.
	 */
	private static void checkOSandConvertContainers() {
		final List<Container> tempContainers = new ArrayList<>(ContainerManager.getContainerList());
		for (int i = 0; i < tempContainers.size(); i++) {
			final Container container = tempContainers.get(i);

			if (container instanceof SshContainer) {
				final Executor executor = container.getNode().createExecutor();
				log.trace("Connecting node executor for checking OS on the machine");
				executor.connect();

				final String os = executor.executeCommandSilently("uname");
				if (StringUtils.containsIgnoreCase(os, "cyg")) {
					// Create JoinContainer from SshContainer
					log.info("Container " + container.getName() + " running on Windows. Converting to join container!");
					final Container joinContainer = JoinContainer.joinBuilder(container).build();

					// Replace parent for all child containers
					final Set<Container> children = ContainerManager.getChildContainers(container);
					for (Container child : children) {
						child.setParent(joinContainer);
						child.setParentName(joinContainer.getName());
					}

					// Remove SshContainer from ContainerList and replace it witj JoinContainer
					ContainerManager.getContainerList().remove(i);
					ContainerManager.getContainerList().add(i, joinContainer);
					((SshContainer) container).setJoinContainer((JoinContainer) joinContainer);
				}
			}
		}
	}
}
