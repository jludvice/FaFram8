package org.jboss.fuse.qa.fafram8.cluster.container;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;

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
		return new ChildBuilder();
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
		// Search the parent by its name
		final Container parent = ContainerManager.getContainer(super.getParentName());
		if (parent == null) {
			throw new FaframException(String.format("Specified parent (%s) of container %s does not exist in container list!",
					super.getParentName(), super.getName()));
		}
		super.setParent(parent);
		super.getParent().getExecutor().executeCommand("container-create-child " + super.getParent().getName() + " " + super.getName());
		super.getParent().getExecutor().waitForProvisioning(this);
		super.setOnline(true);
	}

	@Override
	public void destroy() {
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
	}

	@Override
	public void stop() {
		super.getParent().getExecutor().executeCommand("container-stop " + super.getName());
		log.error("TODO: wait here for the container stop?");
		super.setOnline(false);
	}

	@Override
	public void kill() {
	}

	@Override
	public String executeCommand(String command) {
		return super.getParent().getExecutor().executeCommand("container-connect " + super.getName() + " " + command);
	}

	/**
	 * Child builder class - this class returns the ChildContainer object and it is the only way the child container should be built.
	 */
	public static class ChildBuilder {
		// Container instance
		private Container container;

		/**
		 * Constructor.
		 */
		public ChildBuilder() {
			this(null);
		}

		/**
		 * Constructor.
		 *
		 * @param copy container that will be copied.
		 */
		public ChildBuilder(Container copy) {
			if (copy == null) {
				this.container = new ChildContainer();
			} else {
				this.container = copy;
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
		 * Builds the container.
		 *
		 * @return childcontainer instance
		 */
		public Container build() {
			return new ChildContainer()
					.name(container.getName())
					.user(container.getUser())
					.password(container.getPassword())
					.parent(null)
					.parentName(container.getParentName())
					.node(null)
					.executor(null);
		}
	}
}
