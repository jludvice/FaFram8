package org.jboss.fuse.qa.fafram8.cluster.container;

import static org.jboss.fuse.qa.fafram8.modifier.impl.AccessRightsModifier.setExecutable;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.putProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RandomModifier.changeRandomSource;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RootNameModifier.setRootName;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.manager.LocalNodeManager;
import org.jboss.fuse.qa.fafram8.manager.NodeManager;
import org.jboss.fuse.qa.fafram8.manager.RemoteNodeManager;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.ArrayList;
import java.util.Arrays;

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
		// Check if we should create fabric - if the fabricArgument attribute is null, then the container.withFabric
		// was not called - therefore we need check the system property (set by Fafram.withFabric())
		if (super.getFabricCreateArguments() == null) {
			if (SystemProperty.isFabric()) {
				super.setFabric(true);
				super.setFabricCreateArguments(SystemProperty.getFabric());
			}
		}

		// Create fuse executor
		super.setExecutor(super.createExecutor());

		// Instantiate the node manager based on node.getHost()
		if ("localhost".equals(super.getNode().getHost())) {
			nodeManager = new LocalNodeManager(getExecutor());
		} else {
			// Connect the node executor
			super.getNode().getExecutor().connect();
			nodeManager = new RemoteNodeManager(super.getNode().getExecutor(), super.getExecutor());
		}

		// Add the modifiers
		if (!SystemProperty.skipDefaultUser()) {
			// Add default user which is now fafram/fafram with only role Administrator for more transparent tests
			ModifierExecutor.addModifiers(
					putProperty(super.getNode().getHost(), "etc/users.properties", super.getUser(), super.getPassword() + ",Administrator"));
		}

		ModifierExecutor.addModifiers(setExecutable("bin/karaf", "bin/start", "bin/stop"), setRootName(this, super.getNode().getHost()),
				changeRandomSource());

		if (!SystemProperty.isClean()) {
			nodeManager.clean();
		}

		nodeManager.checkRunningContainer();
		try {
			nodeManager.detectPlatformAndProduct();
			nodeManager.prepareZip();
			nodeManager.unzipArtifact();
			nodeManager.prepareFuse(super.getNode().getHost());
			if (!SystemProperty.suppressStart()) {
				nodeManager.startFuse();
				ContainerManager.patchStandaloneBeforeFabric(this);

				ContainerManager.setupFabric(this);

				ContainerManager.patchFuse(this);
				super.setOnline(true);
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

		log.info("Destroying container " + super.getName());

		if (super.isOnline()) {
			nodeManager.stopAndClean(false);
		}
	}

	@Override
	public void restart() {
		nodeManager.restart();
	}

	@Override
	public void start() {
		nodeManager.startFuse();
		super.setOnline(true);
	}

	@Override
	public void stop() {
		nodeManager.stop();
		super.setOnline(false);
	}

	@Override
	public void kill() {
		nodeManager.kill();
	}

	@Override
	public String executeCommand(String command) {
		return super.getExecutor().executeCommand(command);
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
				this.container = root;
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
			container.setNode(Node.builder().host(host).username(user).password(password).build());

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
			container.setNode(Node.builder().host(host).port(port).username(user).password(password).build());

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
		 * Builds the default root container.
		 *
		 * @return this
		 */
		public RootBuilder defaultRoot() {
			container.setName(SystemProperty.getDefaultRootName());
			container.setUser(SystemProperty.getFuseUser());
			container.setPassword(SystemProperty.getFusePassword());
			container.setNode(Node.builder().host(SystemProperty.getHost()).port(SystemProperty.getHostPort()).username(SystemProperty.getHostUser())
					.password(SystemProperty.getHostPassword()).build());
			container.setCommands(ContainerManager.getCommands());
			container.setBundles(ContainerManager.getBundles());
			return this;
		}

		/**
		 * Builds the container.
		 *
		 * @return rootcontainer instance
		 */
		public Container build() {
			// Check if we should add the fabric attributes
			if (SystemProperty.isFabric()) {
				container.setFabric(true);
				container.setFabricCreateArguments(SystemProperty.getFabric());
			}

			Node node = null;
			if (container.getNode() != null) {
				node = Node.builder().host(container.getNode().getHost()).port(container.getNode().getPort())
						.username(container.getNode().getUsername()).password(container.getNode().getPassword()).build();
			}
			// fuse executor is set when the container is being created
			return new RootContainer()
					.name(container.getName())
					.user(container.getUser())
					.password(container.getPassword())
					.root(true)
					// We need to create a new instance of the node for the cloning case, otherwise all clones
					// would have the same object instance
					.node(node)
					.parent(null)
					.parentName(null)
					.fabric(container.isFabric())
					.fabricCreateArguments(container.getFabricCreateArguments())
					// The same as node
					.commands(new ArrayList<>(container.getCommands())).bundles(new ArrayList<>(container.getBundles()))
					.profiles(new ArrayList<>(container.getProfiles()));
		}
	}
}
