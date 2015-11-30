
package org.jboss.fuse.qa.fafram8.cluster;

import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ChildContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.ContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.SshContainerType;
import org.jboss.fuse.qa.fafram8.cluster.brokers.Broker;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by mmelko on 27/10/15.
 */
public class ContainerBuilder {

	@Getter
	@Setter
	private Container tempContainer = new Container();
	private Broker tempBroker;
	private Node tempNode = new Node();
	private ContainerType tempType;

	@Getter
	@Setter
	private Fafram fafram;

	/**
	 * Builds the container.
	 *
	 * @return new created container
	 */
	public Container build() {
		//if root isn't set use Root from fafram by default
		if ((fafram != null) && (tempContainer.getParentContainer() == null)) {
			this.tempContainer.setParentContainer(fafram.getRootContainer());
		}
		tempContainer.setHostNode(tempNode);
		final Container c = new Container(tempContainer);
		c.setContainerType(tempType);
		tempType.setContainer(c);
		tempContainer.setEnssemble(false);

		return c;
	}

	/**
	 * names the container.
	 *
	 * @param containerName name of the container
	 * @return containerBuilder instance
	 */
	public ContainerBuilder name(String containerName) {
		this.tempContainer.setName(containerName);
		return this;
	}

	/**
	 * Define ssh container.
	 *
	 * @return this - container builder
	 */
	public ContainerBuilder ssh() {
		this.tempType = new SshContainerType();
		return this;
	}

	/**
	 * Define ssh container.
	 *
	 * @param name of the ssh container
	 * @return this - container builder
	 */
	public ContainerBuilder ssh(String name) {
		this.tempType = new SshContainerType();
		return name(name);
	}

	/**
	 * Set the host node for the container.
	 *
	 * @param node host node
	 * @return container builder instance
	 */
	public ContainerBuilder host(Node node) {
		this.tempNode = node;
		return this;
	}

	/**
	 * Define if container is a child container.
	 *
	 * @return this - container builder instance
	 */
	public ContainerBuilder child() {
		this.tempType = new ChildContainerType();
		return this;
	}

	/**
	 * Defineses child container with specified name.
	 * @param name name of the child container
	 * @return this - container builder
	 */
	public ContainerBuilder child(String name) {
		child();
		return name(name);
	}

	/**
	 * Adds profile into profile list for container.
	 *
	 * @param profile added profile
	 * @return this - container builder instance
	 */
	public ContainerBuilder addProfile(String profile) {
		this.tempContainer.getProfiles().add(profile);
		return this;
	}

	/**
	 * specify node instance for the container.
	 *
	 * @param host host of the node
	 * @param username username to node
	 * @param password password to node
	 * @return this - container builder instance
	 */
	public ContainerBuilder nodeSsh(String host, String username, String password) {
		this.tempNode.setHost(host);
		this.tempNode.setUsername(username);
		this.tempNode.setPassword(password);
		return this;
	}

	/**
	 * /**
	 * specify node instance for the container.
	 *
	 * @param host host of the node
	 * @param username username to node
	 * @param password password to node
	 * @param port port;
	 * @return this - container builder instance
	 */
	public ContainerBuilder nodeSsh(String host, String username, String password, int port) {
		this.tempNode.setPort(port);
		return nodeSsh(host, username, password);
	}

	/**
	 * Specify only host on hostNode. This can be used if ssh credentials are already set.
	 *
	 * @param host of the host
	 * @return this - container builder instance
	 */
	public ContainerBuilder nodeSsh(String host) {
		this.tempNode.setHost(host);
		return this;
	}

	/**
	 * Specifies ensemble container.
	 *
	 * @return this - container builder instance.
	 */
	public ContainerBuilder enssemble() {
		this.tempContainer.setEnssemble(true);
		return this;
	}

	/**
	 * Specify env properties for ssh container.
	 *
	 * @param env properties
	 * @return this container builder instance
	 */
	public ContainerBuilder env(String env) {
		this.tempContainer.setEnvProperties(env);
		return this;
	}

	/**
	 * specify path of ssh container.
	 *
	 * @param path of ssh container
	 * @return this - instance of container builder
	 */
	public ContainerBuilder path(String path) {
		this.tempContainer.setPath(path);
		return this;
	}

	/**
	 * Sets the parent container for the contanier.
	 *
	 * @param c parent container
	 * @return this - container builder instance
	 */
	public ContainerBuilder parent(Container c) {
		this.tempContainer.setParentContainer(c);
		return this;
	}

	/**
	 * Creates rootContainerType with properties mapped from SystemProperties.
	 *
	 * @return this - container buil
	 */
	public ContainerBuilder rootWithMappedProperties() {
		this.nodeSsh(SystemProperty.getHost(), SystemProperty.getHostUser(), SystemProperty.getHostPassword(), SystemProperty.getHostPort());
		final RootContainerType type = new RootContainerType(tempContainer);
		type.setUsername(SystemProperty.getFuseUser());
		type.setPassword(SystemProperty.getFusePassword());
		//type.setPort(SystemProperty.);
		this.tempType = type;
		return this;
	}

	/**
	 * Set the temp container as a root.
	 *
	 * @param fuseUser new user for fuse
	 * @param fusePassword new password for new user
	 * @return this - container builder instance
	 */
	public ContainerBuilder root(String fuseUser, String fusePassword) {
		final RootContainerType type = new RootContainerType(tempContainer);
		type.setUsername(fuseUser);
		type.setPassword(fusePassword);
		this.tempType = type;

		return this;
	}

	/**
	 * Adds existing container directly into Fafram8 containers.
	 *
	 * @return this - container builder instance
	 */
	public ContainerBuilder addToFafram() {
		if (this.fafram != null) {
			fafram.getContainerList().add(this.build());
		} else {
			throw new FaframException("No fafram instance found.");
		}
		return this;
	}

	/**
	 * Invokes initContainers from fafram class if isn't null.
	 */
	public void buildAll() {
		if (this.fafram != null) {
			fafram.initContainers();
		} else {
			throw new FaframException("No fafram instance found.");
		}
	}

	/**
	 * Adds  command  which will be executed before  others containers are initialized.
	 *
	 * @param command to be executed.
	 * @return  this
	 */
	public ContainerBuilder addCommand(String command) {
		final RootContainerType rootType = ((RootContainerType) this.tempType);
		rootType.addCommand(command);
		return this;
	}
}
