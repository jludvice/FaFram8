package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.manager.ContainerType;
import org.jboss.fuse.qa.fafram8.manager.Node;

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
	private Node host;

	@Getter
	@Setter
	private ContainerType containerType;

	public void containerCreate(){
		containerType.createContainer(this);
	}
}
