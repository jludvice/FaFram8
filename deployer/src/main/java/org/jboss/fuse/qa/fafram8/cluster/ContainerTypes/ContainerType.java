package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import lombok.ToString;
import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.executor.Executor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Interface represents generic container type.
 * Created by mmelko on 09/10/15.
 */
@ToString
@Slf4j
public abstract class ContainerType {

	@Setter
	@Getter
	protected Container container;

	@Getter
	@Setter
	protected Executor executor;

	/**
	 * Constructor.
	 */
	public ContainerType() {
	}

	/**
	 * Constructor.
	 *
	 * @param container Reference to container
	 */
	public ContainerType(Container container) {
		this.container = container;
	}

	/**
	 * Constructor.
	 *
	 * @param container reference to container
	 * @param executor executor
	 */
	public ContainerType(Container container, Executor executor) {
		this.container = container;
		this.executor = executor;
	}

	/**
	 * Container create used in child and ssh containers.
	 *
	 * @return command for container create
	 */
	protected String standardCreate() {
		if (executor == null) {
			initExecutor();
		}
		executor.executeCommand(getCreateCommand());
		executor.waitForProvisioning(container.getName());

		return getCreateCommand();
	}

	/**
	 * Executing command abstract method.
	 *
	 * @param command to execute
	 * @return output of the executed command
	 */
	public abstract String executeCommand(String command);

	/**
	 * Inits the executor.
	 */
	protected abstract void initExecutor();

	/**
	 * Create new container in cluster.
	 */
	public abstract void createContainer();

	/**
	 * Delete container from the cluster.
	 */
	public abstract void deleteContainer();

	/**
	 * Stop container in the cluster.
	 */
	public abstract void stopContainer();

	/**
	 * Start container in the cluster if it's not running.
	 */
	public abstract void startContainer();

	/**
	 * Creates and returns Create command for container type.
	 *
	 * @return command for creating the container
	 */
	public abstract String getCreateCommand();

	/**
	 * Kills container in the cluster.
	 */
	public void killContainer() {
		log.debug(executor.toString());
		container.executeCommand("exec pkill -9 -f " + container.getName());
	}
}
