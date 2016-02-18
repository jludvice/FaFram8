package org.jboss.fuse.qa.fafram8.cluster.container;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

/**
 * Class representing ssh container. Instances of this class should and can be created just with SshBuilder class.
 * Created by avano on 1.2.16.
 */
@Slf4j
public class SshContainer extends Container {
	/**
	 * Constructor.
	 */
	protected SshContainer() {
	}

	/**
	 * Constructor.
	 *
	 * @param name container name
	 */
	protected SshContainer(String name) {
		super();
		super.setName(name);
		super.setRoot(false);
	}

	/**
	 * Builder getter.
	 *
	 * @return builder instance
	 */
	public static SshBuilder builder() {
		return new SshBuilder(new SshContainer());
	}

	/**
	 * Builder getter.
	 *
	 * @param c container that will be copied
	 * @return builder instance
	 */
	public static SshBuilder builder(Container c) {
		return new SshBuilder(c);
	}

	@Override
	public void create() {
		if (super.getParent() == null) {
			// Search the parent by its name
			final Container parent = ContainerManager.getContainer(super.getParentName());
			if (parent == null) {
				throw new FaframException(String.format("Specified parent (%s) of container %s does not exist in container list!",
						super.getParentName(), super.getName()));
			}
			super.setParent(parent);
		}
		if (SystemProperty.suppressStart()) {
			return;
		}

		String arguments = "";

		for (String profile : super.getProfiles()) {
			arguments += " --profile " + profile;
		}

		if (super.getVersion() != null) {
			arguments += " --version " + super.getVersion();
		}

		super.getParent().getExecutor().executeCommand(String.format("container-create-ssh --user %s --password %s --host %s%s %s",
				super.getNode().getUsername(), super.getNode().getPassword(), super.getNode().getHost(), arguments, super.getName()));
		super.getParent().getExecutor().waitForProvisioning(this);
		super.setOnline(true);
	}

	@Override
	public void destroy() {
		if (SystemProperty.suppressStart()) {
			return;
		}
		log.info("Destroying container " + super.getName());
		super.getParent().getExecutor().executeCommand("container-delete " + super.getName());
	}

	@Override
	public void restart() {
		stop();
		start();
	}

	@Override
	public void start() {
		super.getParent().getExecutor().executeCommand("container-start " + super.getName());
		super.getParent().getExecutor().waitForProvisioning(this);
		super.setOnline(true);
	}

	@Override
	public void stop() {
		super.getParent().getExecutor().executeCommand("container-stop " + super.getName());
		super.getParent().getExecutor().waitForProvisionStatus(this, "stopped");
		super.setOnline(false);
	}

	@Override
	public void kill() {
		// Use the ssh hack here because ssh container does not have public ip
	}

	@Override
	public String executeCommand(String command) {
		return super.getParent().getExecutor().executeCommand(command);
	}

	@Override
	public void waitForProvisioning() {
		waitForProvisionStatus("success");
	}

	@Override
	public void waitForProvisionStatus(String status) {
		super.getParent().getExecutor().waitForProvisionStatus(this, status);
	}

	/**
	 * Ssh builder class - this class returns the SshContainer object and it is the only way the ssh container should be built.
	 */
	public static class SshBuilder {
		// Container instance
		private Container container;

		/**
		 * Constructor.
		 *
		 * @param copy container that will be copied
		 */
		public SshBuilder(Container copy) {
			if (copy != null) {
				this.container = copy;
			} else {
				this.container = new SshContainer();
				// Set the empty node
				container.setNode(Node.builder().build());
			}
		}

		/**
		 * Setter.
		 *
		 * @param name name
		 * @return this
		 */
		public SshBuilder name(String name) {
			container.setName(name);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param user user
		 * @return user
		 */
		public SshBuilder user(String user) {
			container.getNode().setUsername(user);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param password password
		 * @return this
		 */
		public SshBuilder password(String password) {
			container.getNode().setPassword(password);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param node node
		 * @return this
		 */
		public SshBuilder node(Node node) {
			container.setNode(node);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param host host
		 * @return this
		 */
		public SshBuilder node(String host) {
			container.setNode(Node.builder().host(host).build());

			return this;
		}

		/**
		 * Setter.
		 * @param host host
		 * @param user user
		 * @param password password
		 * @return this
		 */
		public SshBuilder node(String host, String user, String password) {
			container.setNode(
					Node.builder()
							.host(host)
							.username(user)
							.password(password)
							.build()
			);

			return this;
		}

		/**
		 * Setter.
		 * @param host host
		 * @param port port
		 * @param user user
		 * @param password password
		 * @return this
		 */
		public SshBuilder node(String host, int port, String user, String password) {
			container.setNode(
					Node.builder()
							.host(host)
							.port(port)
							.username(user)
							.password(password)
							.build()
			);

			return this;
		}

		/**
		 * Setter.
		 *
		 * @param parentName parent name
		 * @return this
		 */
		public SshBuilder parentName(String parentName) {
			container.setParentName(parentName);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param parent parent
		 * @return this
		 */
		public SshBuilder parent(Container parent) {
			container.setParent(parent);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param profiles profiles array
		 * @return this
		 */
		public SshBuilder profiles(String... profiles) {
			container.getProfiles().addAll(Arrays.asList(profiles));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param commands commands array
		 * @return this
		 */
		public SshBuilder commands(String... commands) {
			container.getCommands().addAll(Arrays.asList(commands));
			return this;
		}

		/**
		 * Setter.
		 * @param version version
		 * @return this
		 */
		public SshBuilder version(String version) {
			container.setVersion(version);
			return this;
		}

		/**
		 * Builds the instance.
		 *
		 * @return sshcontainer instance
		 */
		public Container build() {
			return new SshContainer()
					.name(container.getName())
					.user(container.getUser())
					.password(container.getPassword())
					.parent(container.getParent())
					.parentName(container.getParentName())
					// We need to create a new instance of the node for the cloning case, otherwise all clones
					// would have the same object instance
					.node(Node.builder()
							.host(container.getNode().getHost())
							.port(container.getNode().getPort())
							.username(container.getNode().getUsername())
							.password(container.getNode().getPassword())
							.build())
					// Same as node
					.commands(new ArrayList<>(container.getCommands()))
					.profiles(new ArrayList<>(container.getProfiles()))
					.version(container.getVersion());
		}
	}
}
