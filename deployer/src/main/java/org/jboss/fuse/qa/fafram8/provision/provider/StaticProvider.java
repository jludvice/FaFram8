package org.jboss.fuse.qa.fafram8.provision.provider;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exception.OfflineEnvironmentException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

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
		log.info("Server instances specified in configuration.");
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

	@Override
	public void loadIPtables(List<Container> containerList) {
		log.info("Changing environment to offline.");
		SSHClient sshClient;
		Executor executor;
		for (Container c : containerList) {
			sshClient = new NodeSSHClient().defaultSSHPort().hostname(c.getHostNode().getHost())
					.username(c.getHostNode().getUsername()).password(c.getHostNode().getPassword());
			executor = new Executor(sshClient);
			log.debug("Turning off internet on " + executor);
			try {
				executor.connect();
				String response = executor.executeCommand("sudo cat " + SystemProperty.getIptablesConfFilePath());

				if (response.contains("No such file or directory"))
					throw new OfflineEnvironmentException("Configuration file for iptables" +
							" doesn't exists on node: " + c.getHostNode().getHost() + ".",
							new FileNotFoundException("File " + SystemProperty.getIptablesConfFilePath() + " doesn't exists."));

				executor.executeCommand("sudo iptables-restore " + SystemProperty.getIptablesConfFilePath());

				response = executor.executeCommand("wget www.google.com");
				if (response.contains("failed: Connection refused") && response.contains("failed: Network is unreachable."))
					throw new OfflineEnvironmentException("Internet connection wasn't turn off successfully on node: "
							+ c.getHostNode().getHost() + ". Check " + 	SystemProperty.getIptablesConfFilePath()
							+ " file on the node.");
			} catch (SSHClientException e) {
				throw new OfflineEnvironmentException(e);
			}
		}
	}

	@Override
	public void mountStorageOnRootNode(List<Container> containerList) {
		// in static provider it doesn't make sense to mount something by default. Do nothing.
	}
}
