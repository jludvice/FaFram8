package org.jboss.fuse.qa.fafram8.deployer;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.manager.NodeManager;
import org.jboss.fuse.qa.fafram8.manager.RemoteNodeManager;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

/**
 * Remote deployer class.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteDeployer implements Deployer {
	private RemoteNodeManager nm;
	private ContainerManager cm;

	/**
	 * Constructor.
	 *
	 * @param nodeClient sshClient to remote host
	 * @param fuseClient sshClient to fuse on remote host
	 * @throws SSHClientException if there is some serious problem with ssh
	 */
	public RemoteDeployer(SSHClient nodeClient, SSHClient fuseClient) throws SSHClientException {
		this.nm = new RemoteNodeManager(nodeClient, fuseClient);
		this.cm = new ContainerManager(fuseClient);

		// If Clean property is not set then default value is true
		SystemProperty.set(FaframConstant.CLEAN, "true");

		//this.configurationParser = ConfigurationParser.getInstance();
		//TODO(ecervena): consider where parsing of config file should be called
		//this.configurationParser.parseConfigurationFile("path/to/configuration/file");
	}

	@Override
	public void setup() {
		try {
			if (!SystemProperty.getClean()) {
				// If clean is set to false then fafram is only connecting to running Fuse. In this scenario we need to
				// connect ContainerManager to running Fuse to be able to execute commands.
				cm.getExecutor().connect();
				return;
			}

			nm.clean();
			nm.prepareZip();
			nm.unzipArtifact();
			nm.prepareFuse();
			if (!SystemProperty.suppressStart()) {
				nm.startFuse();
				cm.patchStandaloneBeforeFabric();
				if (SystemProperty.isFabric()) {
					cm.setupFabric(nm);
				}
			}
		} catch (FaframException ex) {
			nm.stopAndClean(true);
			throw new FaframException(ex);
		} catch (SSHClientException ex) {
			throw new FaframException(ex);
		}
	}

	@Override
	public void tearDown() {
		SystemProperty.clearAllProperties();
		ModifierExecutor.clearAllModifiers();
	}

	@Override
	public NodeManager getNodeManager() {
		return nm;
	}

	@Override
	public ContainerManager getContainerManager() {
		return cm;
	}
}
