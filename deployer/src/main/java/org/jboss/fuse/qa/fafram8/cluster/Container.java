package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ContainerType;

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

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private Node hostNode;

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

	@Getter
	@Setter
	String path;

	public void containerCreate() {
		containerType.createContainer();
	}
}
