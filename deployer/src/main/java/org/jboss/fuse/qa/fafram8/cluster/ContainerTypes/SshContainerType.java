package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;

/**
 * Created by mmelko on 27/10/15.
 */
public class SshContainerType extends ContainerType {
	@Override
	public String createContainer() throws SSHClientException {
		return null;
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
		return null;
	}
}
