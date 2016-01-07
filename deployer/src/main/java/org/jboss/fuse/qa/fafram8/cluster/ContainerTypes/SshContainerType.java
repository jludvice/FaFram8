package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.cluster.Node;

/**
 * Class represents ssh container type.
 * Created by mmelko on 27/10/15.
 */
public class SshContainerType extends ContainerType {
	@Override
	public void createContainer() {
		if (executor == null) {
			initExecutor();
		}
		executor.executeCommand(getCreateCommand());
		executor.waitForProvisioning(container.getName());
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
		String command;
		//TODO(mmelko): add other parameters.
		command = "container-create-ssh " + getNodeSsh() + " ";

		String profiles = "";
		for (String p : this.container.getProfiles()) {
			profiles += " --profile " + p;
		}

		if (container.getPath() != null && !"".equals(container.getPath())) {
			command += " --path " + container.getPath();
		}

		if (container.getEnvProperties() != null && !"".equals(container.getEnvProperties())) {
			command += " --env " + container.getEnvProperties();
		}

		return command += profiles + " " + container.getName();
	}

	private String getNodeSsh() {
		String nodeSSH;
		final Node host = container.getHostNode();
		//todo(Anybody): ssh privateKey, passhprase

		nodeSSH = "--user " + host.getUsername() + " --host " + host.getHost() + " --password " + host.getPassword();
		return nodeSSH;
	}

	@Override
	public String executeCommand(String command) {
		return executor.executeCommand("container-connect " + this.container.getName() + " " + command);
	}

	@Override
	protected void initExecutor() {
		executor = container.getParentContainer().getContainerType().getExecutor();
	}
}
