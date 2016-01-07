package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

/**
 * Child container type class defines a way how child containers are created,deleted and stopped.
 * Created by mmelko on 27/10/15.
 */
public class ChildContainerType extends ContainerType {

	@Override
	public String executeCommand(String command) {
		return executor.executeCommand("container-connect " + this.container.getName() + " " + command);
	}

	@Override
	protected void initExecutor() {
		executor = container.getParentContainer().getContainerType().getExecutor();
	}

	@Override
	public void createContainer() {
		standardCreate();
	}

	@Override
	public void deleteContainer() {
		executor.executeCommand("container-delete " + container.getName());
	}

	@Override
	public void stopContainer() {
		executor.executeCommand("container-stop " + container.getName());
	}

	@Override
	public void startContainer() {
		executor.executeCommand("container-start " + container.getName());
	}

	@Override
	public String getCreateCommand() {
		String profiles = "";
		for (String p : this.container.getProfiles()) {
			profiles += " --profile " + p;
		}

		return "container-create-child" + profiles + " " + getContainer().getParentContainer()
				.getName() + " " + getContainer().getName();
	}
}
