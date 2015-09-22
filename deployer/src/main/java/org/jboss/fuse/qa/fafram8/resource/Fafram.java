package org.jboss.fuse.qa.fafram8.resource;

import org.jboss.fuse.qa.fafram8.deployer.Deployer;
import org.jboss.fuse.qa.fafram8.deployer.LocalDeployer;
import org.jboss.fuse.qa.fafram8.deployer.RemoteDeployer;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.FuseSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;
import org.jboss.fuse.qa.fafram8.validator.Validator;

import org.junit.rules.ExternalResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Fafram resource class.
 * Created by avano on 19.8.15.
 */
@Slf4j
public class Fafram extends ExternalResource {
	// Deployer instance
	private Deployer deployer;

	/**
	 * Constructor.
	 */
	public Fafram() {
		Validator.validate();
		if (SystemProperty.getHost() == null) {
			log.info("Setting up local deployment");
			setupLocalDeployment();
		} else {
			log.info("Setting up remote deployment on host " + SystemProperty.getHost() + ":" + SystemProperty
					.getHostPort());
			try {
				setupRemoteDeployment();
			} catch (SSHClientException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void before() {
		setup();
	}

	@Override
	protected void after() {
		tearDown();
	}

	/**
	 * Start method.
	 */
	public void setup() {
		// Start deployer
		deployer.setup();
	}

	/**
	 * Stop method.
	 */
	public void tearDown() {
		deployer.tearDown();
	}

	/**
	 * Sets up the local deployment.
	 */
	private void setupLocalDeployment() {
		// Don't use fabric by default on localhost
		System.clearProperty(FaframConstant.FABRIC);

		final int defaultPort = 8101;

		// Create a local deployer with local SSH Client and assign to deployer variable
		deployer = new LocalDeployer(new FuseSSHClient().hostname("localhost").port(defaultPort).username(SystemProperty
				.getFuseUser()).password(SystemProperty.getFusePassword()));
	}

	/**
	 * Sets up the remote deployment.
	 */
	private void setupRemoteDeployment() throws SSHClientException {
		// Use fabric by default on remote
		System.setProperty(FaframConstant.FABRIC, "");
		final SSHClient node = new NodeSSHClient().hostname(SystemProperty.getHost()).port(SystemProperty.getHostPort())
				.username(SystemProperty.getHostUser()).password(SystemProperty.getHostPassword());
		final SSHClient fuse = new FuseSSHClient().hostname(SystemProperty.getHost()).fuseSSHPort().username(
				SystemProperty.getFuseUser()).password(SystemProperty.getFusePassword());
		deployer = new RemoteDeployer(node, fuse);
	}

	/**
	 * Executes a command.
	 *
	 * @param command command
	 * @return command response
	 */
	public String executeCommand(String command) {
		return deployer.getNodeManager().getExecutor().executeCommand(command);
	}

	/**
	 * Executes a command in root container shell.
	 *
	 * @param command fabric command to execute on root container
	 * @return command stdo
	 */
	public String executeFuseCommand(String command) {
		return deployer.getContainerManager().getExecutor().executeCommand(command);
	}

	/**
	 * Adds a new user.
	 *
	 * @param user user
	 * @param password password
	 * @param roles comma-separated roles
	 * @return this
	 */
	public Fafram addUser(String user, String password, String roles) {
		deployer.getNodeManager().addUser(user, password, roles);
		return this;
	}

	/**
	 * Replaces a file.
	 *
	 * @param fileToReplace file to replace
	 * @param fileToUse file to use
	 * @return this
	 */
	public Fafram replaceFile(String fileToReplace, String fileToUse) {
		deployer.getNodeManager().replaceFile(fileToReplace, fileToUse);
		return this;
	}

	/**
	 * Provide deployment with Fabric environment.
	 *
	 * @return this
	 */
	public Fafram withFabric() {
		return withFabric("");
	}

	/**
	 * Provide deployment with Fabric environment.
	 *
	 * @param opts fabric create options
	 * @return this
	 */
	public Fafram withFabric(String opts) {
		System.setProperty(FaframConstant.FABRIC, opts);
		return this;
	}
}
