package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ContainerType;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

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
	}

	@Getter
	@Setter
	private String name;

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

		try {
			containerType.createContainer();
			live = true;
		} catch (SSHClientException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops the container.
	 */
	public void stop() {
		containerType.stopContainer();
	}
}
