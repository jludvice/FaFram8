package org.jboss.fuse.qa.fafram8.provision.provider;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.exception.OfflineEnvironmentException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Default provision provider implementation. This implemenation does nothing. Used in case of pre-configured
 * existing hosts hosts.
 * <p/>
 * Created by ecervena on 13.10.15.
 */
@Slf4j
public class StaticProvider implements ProvisionProvider {
	/**
	 * Does nothing.
	 *
	 * @param containerList list of containers
	 */
	@Override
	public void createServerPool(List<Container> containerList) {
		log.info("Assuming static test infrastructure. Dynamic server provision is skipped.");
		if (SystemProperty.getClean()) {
			log.info("Cleaning resources");
			for (Container c : containerList) {
				//TODO(mmelko): Finish containers cleaning.
			}
		}
	}

	/**
	 * Does nothing.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	@Override
	public void assignAddresses(List<Container> containerList) {
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void releaseResources() {
	}

	/**
	 * //TODO(rjakubco): Create and think on how to clean and return iptables back to normal
	 * This is experimental method for StaticProvider. It should work but after test or failure in setup environment there
	 * is no cleaning method for now for restoring iptables back to default. This will be added late because there is serious
	 * need for refactor.
	 *
	 * @param containerList list of containers
	 */
	@Override
	public void loadIPTables(List<Container> containerList) {
		// "If" for deciding if this method should be used is moved here so the Fafram method is clean(Only for you ecervena <3)
		if (SystemProperty.getIptablesConfFilePath().isEmpty()) {
			// There was no iptables configuration file set so the user doesn't want to change environment.
			return;
		}

		log.info("Loading iptables configuration files.");
		SSHClient sshClient;
		Executor executor;

		for (Container c : containerList) {
			if (c instanceof ChildContainer) {
				//				 If the child container is child then skip. The file will be copied and executed for all ssh containers
				//				 and root. It doesn't make sense to do also for child containers.
				continue;
			}

			sshClient = new NodeSSHClient().defaultSSHPort().hostname(c.getNode().getHost())
					.username(c.getNode().getUsername()).password(c.getNode().getPassword());
			executor = new Executor(sshClient);
			log.debug("Loading on iptables on node: " + executor);
			try {
				executor.connect();

				final String directory = ("".equals(SystemProperty.getWorkingDirectory()))
						? executor.executeCommand("pwd") : SystemProperty.getWorkingDirectory();

				// Path to copied iptables file on remote nodes
				final String remoteFilePath =
						directory + File.separator + StringUtils.substringAfterLast(SystemProperty.getIptablesConfFilePath(), File.separator);

				// Copy iptables configuration file from local to all remote nodes
				((NodeSSHClient) sshClient).copyFileToRemote(SystemProperty.getIptablesConfFilePath(), remoteFilePath);

				final String response = executor.executeCommand("sudo cat " + SystemProperty.getIptablesConfFilePath());

				if (response.contains("No such file or directory")) {
					throw new OfflineEnvironmentException("Configuration file for iptables"
							+ " doesn't exists on node: " + c.getNode().getHost() + ".",
							new FileNotFoundException("File " + SystemProperty.getIptablesConfFilePath() + " doesn't exists."));
				}

				executor.executeCommand("sudo iptables-restore " + SystemProperty.getIptablesConfFilePath());
			} catch (Exception e) {
				throw new OfflineEnvironmentException(e);
			}
		}
	}
}
