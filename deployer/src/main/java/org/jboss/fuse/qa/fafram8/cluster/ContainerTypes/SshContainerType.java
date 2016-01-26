package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import lombok.extern.slf4j.Slf4j;
import org.jboss.fuse.qa.fafram8.cluster.Node;

/**
 * Class represents ssh container type.
 * Created by mmelko on 27/10/15.
 */
@Slf4j
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

		//TODO(mmelko): When profiles is not set null pointer exception is rised. Not the first time you don't care about NPE! 
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
		try {
			executor = container.getParentContainer().getContainerType().getExecutor();
		} catch (Exception ex) {
			log.error(container.getParentContainer().toString());
			log.error(container.getParentContainer().getContainerType().toString());
			log.error(container.getParentContainer().getContainerType().getExecutor().toString());
			throw ex;
		}
	}
}
