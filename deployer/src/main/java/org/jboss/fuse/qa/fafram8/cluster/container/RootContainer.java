package org.jboss.fuse.qa.fafram8.cluster.container;

import static org.jboss.fuse.qa.fafram8.modifier.impl.AccessRightsModifier.setExecutable;
import static org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier.addJvmOptsAndRandomSource;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.putProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RootNameModifier.setRootName;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.manager.LocalNodeManager;
import org.jboss.fuse.qa.fafram8.manager.NodeManager;
import org.jboss.fuse.qa.fafram8.manager.RemoteNodeManager;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.modifier.impl.JvmMemoryModifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Class representing root container. Instances of this class should and can be created just with RootBuilder class, because it sets
 * necessary modifiers, etc.
 * Created by avano on 1.2.16.
 */
@Slf4j
public class RootContainer extends Container {
	// Node manager instance - sets up the container on the host
	private NodeManager nodeManager;

	/**
	 * Constructor.
	 */
	protected RootContainer() {
	}

	/**
	 * Constructor.
	 *
	 * @param name container name
	 */
	protected RootContainer(String name) {
		super();
		super.setName(name);
	}

	/**
	 * Builder getter.
	 *
	 * @return builder instance
	 */
	public static RootBuilder builder() {
		return new RootBuilder(new RootContainer());
	}

	/**
	 * Builder getter.
	 *
	 * @param c container that will be copied
	 * @return builder instance
	 */
	public static RootBuilder builder(Container c) {
		return new RootBuilder(c);
	}

	@Override
	public void create() {
		// Create fuse executor
		super.setExecutor(super.createExecutor());
		log.info("Creating container " + this);

		// Instantiate the node manager based on node.getHost()
		if ("localhost".equals(super.getNode().getHost())) {
			nodeManager = new LocalNodeManager(getExecutor());
		} else {
			// Re-create the executor
			super.getNode().setExecutor(super.getNode().createExecutor());
			// Connect the node executor
			super.getNode().getExecutor().connect();
			nodeManager = new RemoteNodeManager(super.getNode().getExecutor(), super.getExecutor());

			// Set workign directory for root container if it was set on root container object
			// It will be either empty string of file system path
			((RemoteNodeManager) nodeManager).setWorkingDirectory(super.getWorkingDirectory());
		}

		ModifierExecutor.setContainer(this);
		// Add the modifiers
		if (!SystemProperty.skipDefaultUser()) {
			// Add default user which is now fafram/fafram with only role Administrator for more transparent tests
			ModifierExecutor.addModifiers(
					putProperty(super.getNode().getHost(), "etc/users.properties", super.getUser(), super.getPassword() + ",Administrator"));
		}

		if (!super.getJvmMemOpts().isEmpty()) {
			ModifierExecutor.addModifiers(JvmMemoryModifier.setJvmMemOpts(super.getJvmMemOpts()));
		}

		ModifierExecutor.addModifiers(setExecutable("bin/karaf", "bin/start", "bin/stop", "bin/client", "bin/fuse"),
				setRootName(this, super.getNode().getHost()), addJvmOptsAndRandomSource(super.getJvmOpts()));

		if (SystemProperty.isClean()) {
			nodeManager.clean();
		}

		nodeManager.checkRunningContainer();
		try {
			nodeManager.detectPlatformAndProduct();
			nodeManager.prepareZip();
			nodeManager.unzipArtifact(this);
			nodeManager.prepareFuse(super.getNode().getHost());
			if (!SystemProperty.suppressStart()) {
				nodeManager.startFuse();
				ContainerManager.patchStandaloneBeforeFabric(this);

				ContainerManager.setupFabric(this);
				ContainerManager.patchFuse(this);
				ContainerManager.executeStartupCommands(this);
				super.setOnline(true);
				super.setCreated(true);
			}
		} catch (FaframException ex) {
			ex.printStackTrace();
			nodeManager.stopAndClean(true);
			throw new FaframException(ex);
		}
	}

	@Override
	public void destroy() {
		ModifierExecutor.executePostModifiers();

		if (!super.isCreated()) {
			return;
		}

		log.info("Destroying container " + super.getName());

		if (super.isOnline()) {
			nodeManager.stopAndClean(false);
			super.getExecutor().disconnect();
			if (super.getNode().getExecutor() != null && super.getNode().getExecutor().isConnected()) {
				super.getNode().getExecutor().disconnect();
			}
		}
		super.setCreated(false);
		ContainerManager.getContainerList().remove(this);
	}

	@Override
	public void restart(boolean force) {
		// Force not used with root container
		nodeManager.restart();
		if (super.isFabric()) {
			waitForProvisioning();
		}
	}

	@Override
	public void start(boolean force) {
		// Force not used with root container
		nodeManager.startFuse();
		super.setOnline(true);
		if (super.isFabric()) {
			waitForProvisioning();
		}
	}

	@Override
	public void stop(boolean force) {
		// Force not used with root container
		nodeManager.stop();
		super.setOnline(false);
	}

	@Override
	public void kill() {
		nodeManager.kill();
	}

	@Override
	public void waitForProvisioning() {
		waitForProvisionStatus("success");
	}

	@Override
	public void waitForProvisioning(int time) {
		super.getExecutor().waitForProvisioning(this, time);
	}

	@Override
	public void waitForProvisionStatus(String status) {
		super.getExecutor().waitForProvisionStatus(this, status);
	}

	@Override
	public void waitForProvisionStatus(String status, int time) {
		super.getExecutor().waitForProvisionStatus(this, status, time);
	}

	@Override
	public String executeCommand(String command) {
		return super.getExecutor().executeCommand(command);
	}

	@Override
	public List<String> executeCommands(String... commands) {
		return super.getExecutor().executeCommands(commands);
	}

	/**
	 * Root builder class - this class returns the RootContainer object and it is the only way the root container should be built.
	 */
	public static class RootBuilder {
		// Container instance
		private Container container;

		/**
		 * Constructor.
		 *
		 * @param root container that will be copied
		 */
		public RootBuilder(Container root) {
			if (root != null) {
				Node node = null;
				if (root.getNode() != null) {
					node = Node.builder()
							.host(root.getNode().getHost())
							.port(root.getNode().getPort())
							.username(root.getNode().getUsername())
							.password(root.getNode().getPassword())
							.build();
				}

				// fuse executor is set when the container is being created
				this.container = new RootContainer()
						.name(root.getName())
						.user(root.getUser())
						.password(root.getPassword())
						.root(true)
						// We need to create a new instance of the node for the cloning case, otherwise all clones
						// would have the same object instance
						.node(node)
						.parent(null)
						.parentName(null)
						.fabric(root.isFabric())
						.fabricCreateArguments(root.getFabricCreateArguments())
						// The same as node
						.commands(new ArrayList<>(root.getCommands()))
						.bundles(new ArrayList<>(root.getBundles()))
						.profiles(new ArrayList<>(root.getProfiles()))
						.bundles(new ArrayList<>(root.getBundles()))
						.jvmOpts(root.getJvmOpts())
						.jvmMemOpts(root.getJvmMemOpts())
						.directory(root.getWorkingDirectory());
			} else {
				container = new RootContainer();
			}
		}

		/**
		 * Setter.
		 *
		 * @param name name
		 * @return this
		 */
		public RootBuilder name(String name) {
			container.setName(name);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param user user
		 * @return this
		 */
		public RootBuilder user(String user) {
			container.setUser(user);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param password password
		 * @return this
		 */
		public RootBuilder password(String password) {
			container.setPassword(password);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param node node instance
		 * @return this
		 */
		public RootBuilder node(Node node) {
			container.setNode(node);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param host host
		 * @return this
		 */
		public RootBuilder node(String host) {
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
		public RootBuilder node(String host, String user, String password) {
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
		public RootBuilder node(String host, int port, String user, String password) {
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
		 * @return this
		 */
		public RootBuilder withFabric() {
			container.setFabric(true);
			container.setFabricCreateArguments("");

			return this;
		}

		/**
		 * Setter.
		 *
		 * @param args fabric create arguments
		 * @return this
		 */
		public RootBuilder withFabric(String args) {
			container.setFabric(true);
			container.setFabricCreateArguments(args);

			return this;
		}

		/**
		 * Setter.
		 *
		 * @param commands commands array
		 * @return this
		 */
		public RootBuilder commands(String... commands) {
			container.getCommands().addAll(Arrays.asList(commands));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param bundles bundles array
		 * @return this
		 */
		public RootBuilder bundles(String... bundles) {
			container.getBundles().addAll(Arrays.asList(bundles));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param profiles profiles array
		 * @return this
		 */
		public RootBuilder profiles(String... profiles) {
			container.getProfiles().addAll(Arrays.asList(profiles));
			return this;
		}

		/**
		 * Sets the JVM options.
		 *
		 * @param xms xms
		 * @param xmx xmx
		 * @param permMem perm mem
		 * @param maxPermMem max perm mem
		 * @return this
		 */
		public RootBuilder jvmMemoryOpts(String xms, String xmx, String permMem, String maxPermMem) {
			container.getJvmMemOpts().add("JAVA_MIN_MEM=" + xms);
			container.getJvmMemOpts().add("JAVA_MAX_MEM=" + xmx);
			container.getJvmMemOpts().add("JAVA_PERM_MEM=" + permMem);
			container.getJvmMemOpts().add("JAVA_MAX_PERM_MEM=" + maxPermMem);
			return this;
		}

		/**
		 * Sets JVM opts.
		 *
		 * @param jvmOpts JVM options for setting
		 * @return this
		 */
		public RootBuilder jvmOpts(String... jvmOpts) {
			container.getJvmOpts().addAll(Arrays.asList(jvmOpts));
			return this;
		}

		/**
		 * Builds the default root container.
		 *
		 * @return this
		 */
		public RootBuilder defaultRoot() {
			// Check if we should add the fabric attributes
			if (SystemProperty.isFabric() && !container.isFabric()) {
				container.setFabric(true);
				container.setFabricCreateArguments(SystemProperty.getFabric());
			}
			// Create node with default properties but without the host - it will be set later in ContainerManager configure roots
			// because in this moment we don't know if we use local or openstack or anything else
			// Port, user and pass are automatically set in Node object from system properties
			container.setNode(Node.builder().build());
			container.setName(SystemProperty.getDefaultRootName());
			container.setUser(SystemProperty.getFuseUser());
			container.setPassword(SystemProperty.getFusePassword());
			container.setCommands(ContainerManager.getCommands());
			container.setBundles(ContainerManager.getBundles());
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param workingDirectory file path to working directory for SSH container
		 * @return this
		 */
		public RootBuilder directory(String workingDirectory) {
			container.setWorkingDirectory(workingDirectory);
			return this;
		}

		/**
		 * Builds the container.
		 *
		 * @return rootcontainer instance
		 */
		public Container build() {
			// Add zookeeper commands because child/ssh container.stop() need them
			if (container.isFabric()) {
				final String zkCommand = "fabric:profile-edit --feature fabric-zookeeper-commands/0.0.0 default";
				if (!container.getCommands().contains(zkCommand)) {
					container.getCommands().add(zkCommand);
				}
			}
			return container;
		}
	}
}
