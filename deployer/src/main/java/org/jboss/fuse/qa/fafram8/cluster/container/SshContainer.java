package org.jboss.fuse.qa.fafram8.cluster.container;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.Arrays;
import java.util.List;

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
		// Get String containing all properties of container
		String properties = setAndFormatProperties();

		// Find out if system property or parameter on container of working directory was set
		if (!("".equals(super.getWorkingDirectory())) || !("".equals(SystemProperty.getWorkingDirectory()))) {
			// Decide if working directory was set on ssh and if not set the system property as default
			final String path = "".equals(super.getWorkingDirectory()) ? SystemProperty.getWorkingDirectory() : super.getWorkingDirectory();
			log.debug("Working directory was set. Setting working directory \"{}\" for container \"{}\".", path, super.getName());
			properties = properties + " --path " + path;
		}

		log.info("Creating container " + this);

		getExecutor().executeCommand(String.format("container-create-ssh --user %s --password %s --host %s%s %s",
				super.getNode().getUsername(), super.getNode().getPassword(), super.getNode().getHost(), properties, super.getName()));
		super.setCreated(true);
		getExecutor().waitForProvisioning(this);
		super.setOnline(true);
	}

	@Override
	public void destroy() {
		if (SystemProperty.suppressStart() || !super.isCreated()) {
			return;
		}

		log.info("Destroying container " + super.getName());
		getExecutor().executeCommand("container-delete " + super.getName());
		super.setCreated(false);
	}

	@Override
	public void restart() {
		stop();
		start();
	}

	@Override
	public void start() {
		getExecutor().executeCommand("container-start " + super.getName());
		getExecutor().waitForProvisioning(this);
		super.setOnline(true);
	}

	@Override
	public void stop() {
		getExecutor().executeCommand("container-stop " + super.getName());
		getExecutor().waitForContainerStop(this);
		super.setOnline(false);
	}

	@Override
	public void kill() {
		super.getParent().getNode().getExecutor().executeCommand("ssh -o StrictHostKeyChecking=no " + super.getNode().getUsername() + "@"
				+ super.getNode().getHost() + " pkill -9 -f containers/" + super.getName());
	}

	@Override
	public String executeCommand(String command) {
		return getExecutor().executeCommand("container-connect " + super.getName() + " " + command);
	}

	@Override
	public List<String> executeCommands(String... commands) {
		return getExecutor().executeCommands(commands);
	}

	@Override
	public void waitForProvisioning() {
		waitForProvisionStatus("success");
	}

	@Override
	public void waitForProvisionStatus(String status) {
		getExecutor().waitForProvisionStatus(this, status);
	}

	@Override
	public Executor getExecutor() {
		return super.getParent().getExecutor();
	}

	/**
	 * Creates string containing all container's properties correctly formatted for Fuse command.
	 *
	 * @return formatted string containing container's properties
	 */
	private String setAndFormatProperties() {
		final StringBuilder arguments = new StringBuilder("");
		if (!super.getJvmOpts().isEmpty()) {
			final StringBuilder jvmOpts = new StringBuilder(" --jvm-opts \"");
			for (String rule : super.getJvmOpts()) {
				jvmOpts.append(" " + rule);
			}
			jvmOpts.append("\"");
			arguments.append(jvmOpts.toString());
		}

		if (!super.getEnvs().isEmpty()) {
			for (String rule : super.getEnvs()) {
				arguments.append(" --env " + rule);
			}
		}

		if (!SystemProperty.getJavaHome().isEmpty()) {
			arguments.append(" --env JAVA_HOME=" + SystemProperty.getJavaHome());
		}

		for (String profile : super.getProfiles()) {
			arguments.append(" --profile " + profile);
		}

		if (super.getVersion() != null) {
			arguments.append(" --version " + super.getVersion());
		}

		return arguments.toString();
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
		 *
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
		 *
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
		 *
		 * @param version version
		 * @return this
		 */
		public SshBuilder version(String version) {
			container.setVersion(version);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param envs environment variables
		 * @return this
		 */
		public SshBuilder env(String... envs) {
			container.getEnvs().addAll(Arrays.asList(envs));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param jvmOpts JVM options
		 * @return this
		 */
		public SshBuilder jvmOpts(String... jvmOpts) {
			container.getJvmOpts().addAll(Arrays.asList(jvmOpts));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param workingDirectory file path to working directory for SSH container
		 * @return this
		 */
		public SshBuilder directory(String workingDirectory) {
			container.setWorkingDirectory(workingDirectory);
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
					.commands(container.getCommands())
					.profiles(container.getProfiles())
					.version(container.getVersion())
					.jvmOpts(container.getJvmOpts())
					.env(container.getEnvs())
					.directory(container.getWorkingDirectory());
		}
	}
}
