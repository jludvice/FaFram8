package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

/**
 * Created by mmelko on 27/10/15.
 */
public class SshContainerType extends ContainerType {
	@Override
	public String createContainer() throws SSHClientException {
		if (executor == null) {
			initExexutor();
		}
		executor.executeCommand(getCreateCommand());
		executor.waitForProvisioning(container.getName());

		return getCreateCommand();
	}

	@Override
	public void deleteContainer() {
	}

	@Override
	public void stopContainer() {
	}

	@Override
	public void startContainer() {
	}

	@Override
	public String getCreateCommand() {
		String command;
		//TODO(mmelko): ostatne veci
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
		//todo(hockto): ssh privateKey, passhprase

		nodeSSH = "--user " + host.getUsername() + " --host " + host.getHost() + " --password " + host.getPassword();
		return nodeSSH;
	}

	@Override
	protected void initExexutor() {
		executor = container.getParentContainer().getContainerType().getExecutor();
	}
}
