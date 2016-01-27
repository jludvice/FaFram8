package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.SshContainerType;
import org.jboss.fuse.qa.fafram8.cluster.xml.ContainerModel;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class representing FUSE container.
 * Created by ecervena on 9/8/15.
 */
@ToString
public class Container {

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
	//TODO(mmelko): You are requestig non initialized profiles list at org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.SshContainerType.getCreateCommand(SshContainerType.java:40) which leads to NPE
	private ArrayList<String> profiles = new ArrayList<>();

	@Getter
	@Setter
	private String path;

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

	/**
	 * Constructor used to create container object from XML container model holder object.
	 *
	 * @param containerModel container model object parsef from Fafram8 XML configuration.
	 */
	//TODO(ecervena): Finish constructor implementation and refactor ConfigurationParser.buildContainers()
	public Container(ContainerModel containerModel) {
		this.name = containerModel.getName();
		switch (containerModel.getContainerType()) {
			case "root": {
				this.containerType = new RootContainerType(this);
			}
			case "ssh": {
				this.containerType = new SshContainerType(this);
			}
			default: {
				break;
			}
		}
	}

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
	 * Delete the container.
	 */
	public void delete() {
		containerType.deleteContainer();
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
	 * Wait for provision of given container.
	 *
	 * @param containerName name of container
	 */
	public void waitForProvision(String containerName) {
		containerType.getExecutor().waitForProvisioning(containerName);
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

	/**
	 * Kills the container.
	 */
	public void killContainer() {
		this.containerType.killContainer();
	}
}
