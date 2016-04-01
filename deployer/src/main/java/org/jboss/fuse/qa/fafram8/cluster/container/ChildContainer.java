package org.jboss.fuse.qa.fafram8.cluster.container;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Class representing child container. Instances of this class should and can be created just with ChildBuilder class.
 * Created by avano on 1.2.16.
 */
@Slf4j
public class ChildContainer extends Container {
	/**
	 * Constructor.
	 */
	protected ChildContainer() {
	}

	/**
	 * Constructor.
	 *
	 * @param name container name
	 */
	protected ChildContainer(String name) {
		super();
		super.setName(name);
		super.setRoot(false);
	}

	/**
	 * Builder getter.
	 *
	 * @return builder instance
	 */
	public static ChildBuilder builder() {
		return new ChildBuilder(new ChildContainer());
	}

	/**
	 * Builder getter.
	 *
	 * @param c container that will be copied
	 * @return builder instance
	 */
	public static ChildBuilder builder(Container c) {
		return new ChildBuilder(c);
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

		final StringBuilder arguments = new StringBuilder("");

		for (String profile : super.getProfiles()) {
			arguments.append(" --profile ").append(profile);
		}

		if (super.getVersion() != null) {
			arguments.append(" --version ").append(super.getVersion());
		}

		if (!super.getJvmOpts().isEmpty()) {
			final StringBuilder jvmOpts = new StringBuilder(" --jvm-opts \"");
			for (String rule : super.getJvmOpts()) {
				jvmOpts.append(" ").append(rule);
			}
			jvmOpts.append("\"");
			arguments.append(jvmOpts.toString());
		}

		log.info("Creating container " + this);

		getExecutor().executeCommand(String.format("container-create-child%s %s %s", arguments.toString(), super.getParent().getName(), super.getName()));
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
	}

	@Override
	public void stop() {
		getExecutor().executeCommand("container-stop " + super.getName());
		getExecutor().waitForContainerStop(this);
		super.setOnline(false);
	}

	@Override
	public void kill() {
		super.getParent().getExecutor().executeCommand("exec pkill -9 -f " + super.getName());
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
	public void waitForProvisioning(int time) {
		getExecutor().waitForProvisioning(this, time);
	}

	@Override
	public void waitForProvisionStatus(String status) {
		getExecutor().waitForProvisionStatus(this, status);
	}

	@Override
	public void waitForProvisionStatus(String status, int time) {
		getExecutor().waitForProvisionStatus(this, status, time);
	}

	@Override
	public Executor getExecutor() {
		return super.getParent().getExecutor();
	}

	/**
	 * Child builder class - this class returns the ChildContainer object and it is the only way the child container should be built.
	 */
	public static class ChildBuilder {
		// Container instance
		private Container container;

		/**
		 * Constructor.
		 *
		 * @param copy container that will be copied.
		 */
		public ChildBuilder(Container copy) {
			if (copy == null) {
				this.container = new ChildContainer();
			} else {
				this.container = new ChildContainer()
						.name(copy.getName())
						.user(copy.getUser())
						.password(copy.getPassword())
						.parent(copy.getParent())
						.parentName(copy.getParentName())
						.profiles(new ArrayList<>(copy.getProfiles()))
						.commands(new ArrayList<>(copy.getCommands()))
						.version(copy.getVersion())
						.jvmOpts(copy.getJvmOpts())
						.node(null);
			}
		}

		/**
		 * Setter.
		 *
		 * @param name name
		 * @return this
		 */
		public ChildBuilder name(String name) {
			container.setName(name);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param parent parent
		 * @return this
		 */
		public ChildBuilder parent(Container parent) {
			container.setParent(parent);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param parent parent container name
		 * @return this
		 */
		public ChildBuilder parentName(String parent) {
			container.setParentName(parent);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param profiles profiles array
		 * @return this
		 */
		public ChildBuilder profiles(String... profiles) {
			container.getProfiles().addAll(Arrays.asList(profiles));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param commands commands array
		 * @return this
		 */
		public ChildBuilder commands(String... commands) {
			container.getCommands().addAll(Arrays.asList(commands));
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param version version
		 * @return this
		 */
		public ChildBuilder version(String version) {
			container.setVersion(version);
			return this;
		}

		/**
		 * Setter.
		 *
		 * @param jvmOpts JVM options
		 * @return this
		 */
		public ChildBuilder jvmOpts(String... jvmOpts) {
			container.getJvmOpts().addAll(Arrays.asList(jvmOpts));
			return this;
		}

		/**
		 * Builds the container.
		 *
		 * @return childcontainer instance
		 */
		public Container build() {
			return this.container;
		}
	}
}
