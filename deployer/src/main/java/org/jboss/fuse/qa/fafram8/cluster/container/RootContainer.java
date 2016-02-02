package org.jboss.fuse.qa.fafram8.cluster.container;

import static org.jboss.fuse.qa.fafram8.modifier.impl.AccessRightsModifier.setExecutable;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.putProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RandomModifier.changeRandomSource;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RootNameModifier.setRootName;

import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.manager.LocalNodeManager;
import org.jboss.fuse.qa.fafram8.manager.NodeManager;
import org.jboss.fuse.qa.fafram8.manager.RemoteNodeManager;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.FuseSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

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
		return new RootBuilder(null);
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
		// Instantiate the node manager based on node.getHost()
		if ("localhost".equals(super.getNode().getHost())) {
			nodeManager = new LocalNodeManager(getExecutor());
		} else {
			nodeManager = new RemoteNodeManager(getNode().getExecutor(), getExecutor());
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
				if (SystemProperty.isFabric()) {
					ContainerManager.setupFabric(this);
				}
				ContainerManager.patchFuse(this);

				super.setOnline(true);
			}
		} catch (FaframException ex) {
			nodeManager.stopAndClean(true);
			throw new FaframException(ex);
		}
	}

	@Override
	public void destroy() {
		nodeManager.stopAndClean(false);
	}

	@Override
	public void restart() {
		nodeManager.restart();
	}

	@Override
	public void start() {
		nodeManager.startFuse();
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
		 * @param commands commands array
		 * @return this
		 */
		public RootBuilder commands(String... commands) {
			container.setCommands(Arrays.asList(commands));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param bundles bundles array
		 * @return this
		 */
		public RootBuilder bundles(String... bundles) {
			container.setBundles(Arrays.asList(bundles));
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
			container.setNode(Node.builder()
					.host(SystemProperty.getHost())
					.port(SystemProperty.getHostPort())
					.username(SystemProperty.getHostUser())
					.password(SystemProperty.getHostPassword())
					.build());
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
			// Create the necessary executors
			final SSHClient fuseClient = new FuseSSHClient()
					.hostname(container.getNode().getHost())
					.fuseSSHPort()
					.username(container.getUser())
					.password(container.getPassword());

			// Add the modifiers
			if (!SystemProperty.skipDefaultUser()) {
				// Add default user which is now fafram/fafram with only role Administrator for more transparent tests
				ModifierExecutor.addModifiers(putProperty(container.getNode().getHost(), "etc/users.properties", container.getUser(),
						container.getPassword() + ",Administrator"));
			}

			ModifierExecutor.addModifiers(
					setExecutable("bin/karaf", "bin/start", "bin/stop"),
					setRootName(container),
					changeRandomSource()
			);

			return new RootContainer()
					.name(container.getName())
					.user(container.getUser())
					.password(container.getPassword())
					.executor(new Executor(fuseClient))
					.root(true)
					.node(container.getNode())
					.parent(null)
					.parentName(null)
					.commands(container.getCommands())
					.bundles(container.getBundles());
		}
	}
}
