package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

/**
 * Created by mmelko on 27/10/15.
 */
public class ChildContainerType extends ContainerType {

	@Override
	protected void initExexutor() {
		executor = container.getParentContainer().getContainerType().getExecutor();
	}

	@Override
	public String createContainer() throws SSHClientException {
		return standardCreate();
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
		String profiles = "";
		for (String p : this.container.getProfiles()) {
			profiles += " --profile " + p;
		}

		return "container-create-child" + profiles + " " + getContainer().getParentContainer().getName() + " " + getContainer().getName();
	}
}
