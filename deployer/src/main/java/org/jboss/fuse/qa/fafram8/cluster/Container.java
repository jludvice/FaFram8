package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

/**
 * Class representing FUSE container.
 * Created by ecervena on 9/8/15.
 */
public class Container {
	/**
	 * Constructor.
	 *
	 * @param name name
	 */
	public Container(String name) {
		this.name = name;
	}

	/**
	 * Constructor.
	 */
	public Container() {
		this.profiles = new ArrayList<>();
	}

	/**
	 * Constructor.
	 *
	 * @param container container according which will be new container cloned
	 */
	public Container(Container container) {
		this.name = container.getName();
		this.hostNode = new Node(container.getHostNode());
		this.containerType = container.getContainerType();
		this.parentContainer = container.getParentContainer();
		this.resolver = container.getResolver();
		this.envProperties = container.getEnvProperties();
		this.profiles = new ArrayList<>(container.getProfiles());
		this.path = container.getPath();
		this.ensemble = container.isEnsemble();
	}

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private boolean ensemble = false;

	@Getter
	@Setter
	private Node hostNode;

	@Getter
	@Setter
	private boolean live = false;

	@Getter
	@Setter
	private ContainerType containerType;

	@Getter
	@Setter
	private Container parentContainer;

	@Getter
	@Setter
	private String resolver;

	@Setter
	@Getter
	private String envProperties;

	@Setter
	@Getter
	private ArrayList<String> profiles;

	@Getter
	@Setter
	private String path;

	/**
	 * Method for container creation according container type.
	 */
	public void create() {
		containerType.createContainer();
		live = true;
	}

	/**
	 * Stops the container.
	 */
	public void stop() {
		containerType.stopContainer();
	}

	/**
	 * Clarify if container is root.
	 *
	 * @return true if container is root
	 */
	public boolean isRoot() {
		return containerType instanceof RootContainerType;
	}

	/**
	 * Wait for provision of container.
	 */
	public void waitForProvision() {
		containerType.getExecutor().waitForProvisioning(name);
	}

	/**
	 * Executes command on container.
	 *
	 * @param command to execute
	 * @return output of eecuted command
	 */
	public String executeCommand(String command) {
		return this.containerType.executeCommand(command);
	}
}
