package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.executor.Executor;

import lombok.Getter;
import lombok.Setter;

/**
 * Interface represents generic container type.
 * Created by mmelko on 09/10/15.
 */
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
	 * @param container Reference to container
	 */
	public ContainerType(Container container) {
		super();
		this.container = container;
	}

	/**
	 * Constructor.
	 * @param container reference to container
	 * @param executor executor
	 */
	public ContainerType(Container container, Executor executor) {
		super();
		this.container = container;
		this.executor = executor;
	}

	/**
	 * create new container in cluster.
	 * @return string status
	 * @throws SSHClientException exception
	 */
	public abstract String createContainer() throws SSHClientException;

	/**
	 * Delete container from the cluster.
	 */
	public abstract void deleteContainer();

	/**
	 * stop container in the cluster.
	 */
	public abstract void stopContainer();

	/**
	 * start container in the cluster if it's not running.
	 */
	public abstract void startContainer();

	/**
	 * creates and returns Create command for container type.
	 *
	 * @return  command for creating the container
	 */
	public abstract String getCreateCommand();
}
