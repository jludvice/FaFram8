package org.jboss.fuse.qa.fafram8.executor;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exceptions.CopyFileException;
import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.exceptions.VerifyFalseException;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;
import org.jboss.fuse.qa.fafram8.util.CommandHistory;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor class. This class servers as a wrapper around ssh client and offers methods for waiting for karaf
 * startup, waiting for successful provision, etc.
 * Created by avano on 19.8.15.
 */
@AllArgsConstructor
@Slf4j
@ToString
public class Executor {
	private SSHClient client;

	/**
	 * Executes a command.
	 *
	 * @param cmd command
	 * @return command response
	 */
	@SuppressWarnings("TryWithIdenticalCatches")
	public String executeCommand(String cmd) {
		try {
			final String response = client.executeCommand(cmd, false);
			CommandHistory.add(cmd, response);
			return response;
		} catch (KarafSessionDownException e) {
			log.error("Karaf session is down!");
		} catch (SSHClientException e) {
			log.error("SSHClient exception thrown: " + e);
		}

		return null;
	}

	/**
	 * Checks if the client can connect.
	 *
	 * @return true/false if can/couldn't connect
	 */
	public boolean canConnect() {
		try {
			// We just check if its possible to connect - suppress exception
			client.connect(true);
			client.disconnect();
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Connects client to specified remote server.
	 */
	public void connect() {
		log.debug("Connecting: " + this.toString());
		Boolean connected = false;
		final int step = 1;
		int elapsed = 0;
		final long timeout = step * 1000L;

		log.info("Waiting for SSH connection ...");
		while (!connected) {
			// Check if the time is up
			if (elapsed > SystemProperty.getStartWaitTime()) {
				log.error("Connection couldn't be established after " + SystemProperty.getStartWaitTime()
						+ " seconds");
				throw new FaframException("Connection couldn't be established after "
						+ SystemProperty.getStartWaitTime() + " seconds");
			}
			try {
				client.connect(false);
				connected = true;
				log.info("Connected to remote SSH server (node/fuse)");
			} catch (VerifyFalseException ex) {
				log.debug("Remaining time: " + (SystemProperty.getStartWaitTime() - elapsed) + " seconds. ");
				elapsed += step;
			} catch (SSHClientException e) {
				throw new FaframException("Error while waiting for ssh connection! " + e);
			}
			sleep(timeout);
		}
	}

	/**
	 * Waits for the container to boot.
	 */
	public void waitForBoot() {
		final int step = 3;
		final long timeout = step * 1000L;
		boolean online = false;

		int elapsed = 0;

		while (!online) {
			// Check if the time is up
			if (elapsed > SystemProperty.getStartWaitTime()) {
				log.error("Connection couldn't be established after " + SystemProperty.getStartWaitTime()
						+ " seconds");
				throw new FaframException("Connection couldn't be established after "
						+ SystemProperty.getStartWaitTime() + " seconds");
			}

			try {
				// Try to execute the command - if it throws an exception, it is not ready yet
				// Suppress the exception here to reduce spam
				client.connect(true);
				online = true;
				log.info("Container online");
			} catch (Exception ex) {
				log.debug("Remaining time: " + (SystemProperty.getStartWaitTime() - elapsed) + " seconds. ");
				elapsed += step;
			}
			sleep(timeout);
		}
	}

	/**
	 * Waits for the broker start.
	 */
	public void waitForBroker() {
		final int step = 3;
		final long timeout = step * 1000L;
		boolean online = false;

		int elapsed = 0;

		log.info("Waiting for the broker to be online");

		while (!online) {
			// Check if the time is up
			if (elapsed > SystemProperty.getBrokerStartWaitTime()) {
				log.error("Broker wasn't started after " + SystemProperty.getBrokerStartWaitTime() + " seconds");
				throw new FaframException(
						"Broker wasn't started after " + SystemProperty.getBrokerStartWaitTime() + " seconds");
			}

			String response = null;
			try {
				response = client.executeCommand("activemq:bstat", true);
			} catch (Exception ignored) {
				// Do nothing
			}

			if (StringUtils.contains("BrokerName", response)) {
				log.debug("Remaining time: " + (SystemProperty.getBrokerStartWaitTime() - elapsed) + " seconds. ");
				elapsed += step;
			} else {
				online = true;
				log.info("Broker online");
			}

			sleep(timeout);
		}
	}

	/**
	 * Waits for the container to shut down.
	 */
	public void waitForShutdown() {
		final int step = 5;
		final long timeout = (step * 1000L);
		boolean online = true;

		int elapsed = 0;

		while (online) {
			// Check if the time is up
			if (elapsed > SystemProperty.getStopWaitTime()) {
				log.error("Connection could be established after " + SystemProperty.getStopWaitTime() + " seconds");
				throw new FaframException(
						"Connection could be established after " + SystemProperty.getStopWaitTime() + " seconds");
			}

			try {
				// Check if we are still connected
				online = client.isConnected();
				log.debug("Remaining time: " + (SystemProperty.getStopWaitTime() - elapsed) + " seconds. ");
				elapsed += step;
			} catch (Exception ex) {
				online = false;
			}

			sleep(timeout);
		}
	}

	/**
	 * Waits for the provisioning of a container.
	 *
	 * @param containerName container name
	 */
	public void waitForProvisioning(String containerName) {
		waitForProvisioning(containerName, null);
	}

	/**
	 * Waits for container provisioning. It may restart the container if the provision status is "requires full restart"
	 * or if the provision status contains "NoNodeException"
	 *
	 * @param c Container instance
	 */
	public void waitForProvisioning(Container c) {
		waitForProvisioning(null, c);
	}

	/**
	 * General wait for provisioning method, called by the others. Waits for provisioning of container/container name.
	 *
	 * @param containerName container name
	 * @param c container
	 */
	public void waitForProvisioning(String containerName, Container c) {
		final int step = 3;
		final long timeout = step * 1000L;
		final long startTimeout = 10000L;
		final int maxLength = 6;

		final String waitFor = c == null ? containerName : c.getName();

		// Wait before executing - sometimes the provision is triggered a bit later
		sleep(startTimeout);
		int retries = 0;
		String container;
		String provisionStatus = "";
		boolean isSuccessful = false;
		boolean restarted = false;

		while (!isSuccessful) {
			if (retries > SystemProperty.getProvisionWaitTime()) {
				log.error("Container " + waitFor + " failed to provision in time");
				throw new FaframException("Container " + waitFor + " failed to provision in time");
			}

			String reason = "";

			try {
				container = client.executeCommand("container-list | grep " + waitFor, true);
				isSuccessful = container.contains("success");

				// Parse the provision status from the container-list output
				container = container.replaceAll(" +", " ").trim();
				final String[] content = container.split(" ", maxLength);
				if (content.length == maxLength) {
					provisionStatus = content[maxLength - 1];
				}
			} catch (Exception e) {
				// Get the reason
				reason = e.getMessage();

				// Re-init the ssh connection if it's not successful
				try {
					client.connect(true);
				} catch (Exception ignored) {
				}
			}

			if (("requires full restart".equals(provisionStatus) || provisionStatus.contains("NoNodeException")
					|| provisionStatus.contains("Client is not started")) && c != null) {
				restarted = true;
				log.info("Container requires restart (provision status: " + provisionStatus + ")! Restarting ...");
				break;
			}

			if (!isSuccessful) {
				log.debug("Remaining time: " + (SystemProperty.getProvisionWaitTime() - retries) + " seconds. " + (""
						.equals(reason) ? "" : "(" + reason + ")") + ("".equals(provisionStatus) ? "" : "("
						+ provisionStatus + ")"));
				retries += step;
				provisionStatus = "";
				sleep(timeout);
			}
		}

		// If the container was restarted during the provisioning, trigger the provisioning again
		if (restarted) {
			log.error("TODO: add maximum amount of reprovisioning");
			c.restart();
			waitForProvisioning(c);
		}
	}

	/**
	 * Copies local file to specified location in Fuse folder on remote host.
	 *
	 * @param localPath absolute path to the file on local machine that should be copied
	 * @param remotePath path to destination inside Fuse folder where the file should be copied
	 * @throws CopyFileException if there was error in copying file
	 */
	public void copyFileToRemote(final String localPath, final String remotePath) throws CopyFileException {
		if (client instanceof NodeSSHClient) {
			((NodeSSHClient) client).copyFileToRemote(localPath, remotePath);
		} else {
			throw new CopyFileException("SSH client assigned to Executor is not instance of NodeSSHClient!");
		}
	}

	/**
	 * Waits for the patch defined status.
	 *
	 * @param patchName patch name
	 * @param status status of patch to wait for
	 */
	public void waitForPatchStatus(String patchName, boolean status) {
		final int step = 3;
		final long timeout = step * 1000L;
		int retries = 0;
		boolean isSuccessful = false;

		log.info("Waiting for patch to be installed");

		while (!isSuccessful) {
			sleep(timeout);
			retries += step;
			boolean shouldReconnect = false;
			if (retries > SystemProperty.getPatchWaitTime()) {
				log.error("Container failed to install patch after " + SystemProperty.getPatchWaitTime() + " seconds.");

				final String action = "true".equals(String.valueOf(status)) ? "install" : "rollback";
				log.error("Standalone container failed to " + action + " patch after "
						+ SystemProperty.getPatchWaitTime() + " seconds.");
				throw new FaframException(
						"Container failed to " + action + " patch after " + SystemProperty.getPatchWaitTime() + " seconds.");
			}

			String reason = "";

			try {
				isSuccessful =
						client.executeCommand("patch:list | grep " + patchName, true).contains(String.valueOf(status));
			} catch (Exception e) {
				reason = e.getMessage();
				shouldReconnect = true;
			}

			if (!isSuccessful) {
				log.debug("Remaining time: " + (SystemProperty.getPatchWaitTime() - retries) + " seconds. " + (""
						.equals(reason) ? "" : "(" + reason + ")"));
			}

			if (shouldReconnect) {
				try {
					client.connect(true);
				} catch (Exception ignored) {
				}
			}
		}
	}

	/**
	 * Sleeps for given amount of time.
	 *
	 * @param time time in millis
	 */
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets all the child containers and returns all their names.
	 * <p/>
	 * TODO(avano): this could be reworked in the future after the changes to deploying
	 *
	 * @return list of child container names
	 */
	public List<String> listChildContainers() {
		// I don't want this to be spammed in the log / added to history, therefore I'm using client instead of the executeCommand method
		final List<String> childs = new ArrayList<>();
		try {
			final String containerListResponse = client.executeCommand("container-list | grep -v root | grep karaf", true);

			if (containerListResponse == null) {
				return childs;
			}

			final String[] childContainerList = containerListResponse.split("\n");

			for (String line : childContainerList) {
				final String containerName = line.trim().split(" ")[0];
				// Ssh container is also listed as root
				final boolean isChild = client.executeCommand("container-info " + containerName, true).replaceAll(" +", " ")
						.contains("Root: false");
				if (isChild) {
					childs.add(containerName);
				}
			}
		} catch (Exception ex) {
			log.error("Error while getting child container list! " + ex);
		}

		return childs;
	}
}
