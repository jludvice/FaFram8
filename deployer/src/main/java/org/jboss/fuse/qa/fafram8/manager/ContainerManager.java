package org.jboss.fuse.qa.fafram8.manager;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exception.EmptyContainerListException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.patcher.Patcher;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Container manager class.
 * Created by avano on 2.9.15.
 */
@Slf4j
public class ContainerManager {
	@Getter
	private Executor executor;

	/**
	 * Constructor.
	 *
	 * @param client ssh client
	 */
	public ContainerManager(SSHClient client) {
		this.executor = new Executor(client);
	}

	/**
	 * Sets up fabric.
	 */
	public void setupFabric() {
		executor.executeCommand("fabric:create " + SystemProperty.getFabric());
		try {
			executor.waitForProvisioning("root");
		} catch (FaframException ex) {
			// Container is not provisioned in time
			throw new FaframException("Container did not provision in time");
		}
	}

	/**
	 * Patch fuse.
	 */
	public void patchFuse() {
		if (SystemProperty.getPatch() != null) {
			if (SystemProperty.isFabric()) {
				patchFabric();
			} else {
				patchStandalone();
			}
		}
	}

	/**
	 * Patches the standalone before fabric creation.
	 */
	public void patchStandaloneBeforeFabric() {
		if (SystemProperty.patchStandalone()) {
			patchStandalone();
		}
	}

	/**
	 * Patch standalone container.
	 */
	private void patchStandalone() {
		for (String s : Patcher.getPatches()) {
			final String patchName = getPatchName(executor.executeCommand("patch:add " + s));
			executor.executeCommand("patch:install " + patchName);
			executor.waitForPatchStatus(patchName, true);
		}
	}

	/**
	 * Patch fabric.
	 */
	private void patchFabric() {
		// Create a new version
		final String version = executor.executeCommand("version-create").split(" ")[2];

		for (String s : Patcher.getPatches()) {
			executor.executeCommand("patch-apply -u " + SystemProperty.getFuseUser() + " -p " + SystemProperty
					.getFusePassword() + " --version " + version + " " + s);
		}

		executor.executeCommand("container-upgrade " + version + " root");
		executor.waitForProvisioning("root");
		executor.executeCommand("version-set-default " + version);
	}

	/**
	 * Gets the patch name from the patch-add response.
	 *
	 * @param patchAddResponse patch-add command response.
	 * @return patch name
	 */
	private String getPatchName(String patchAddResponse) {
		// Get the 2nd row only
		String response = StringUtils.substringAfter(patchAddResponse, System.lineSeparator());
		// Replace multiple whitespaces
		response = response.replaceAll(" +", " ").trim();

		// Get the first string in this line
		final String patchName = response.split(" ")[0];
		log.debug("Patch name is " + patchName);
		return patchName;
	}

//	/**
//	 * Execute container-create-ssh command on root container.
//	 *
//	 * @param hostIP IP address of host node
//	 * @param containerName Name of container
//	 */container
	//TODO(ecervena): throw authentization fail exception, implement parallel container spawn
//	private void createSSHContainer(String hostIP, String containerName) {
//		final String command = String.format("container-create-ssh --host %s --user %s --password %s --resolver %s %s",
//				hostIP, SystemProperty.getHostUser(), SystemProperty.getHostPassword(), "localip", containerName);
//		executor.executeCommand(command);
//		executor.waitForProvisioning(containerName);
//	}
//
//	/**
//	 * Execute container-create-ssh command for all containers on the list.
//	 *
//	 * @param containerList container list
//	 * @throws EmptyContainerListException when the container list is empty
//	 */
//	public void createSSHContainer(List<Container> containerList) throws EmptyContainerListException {
//		if (containerList.isEmpty()) {
//			throw new EmptyContainerListException(
//					"List of containers is empty. Root container should be provided in configuration file at least.");
//		}
//		for (Container container : containerList) {
//			if (!container.isRoot()) {
//				createSSHContainer(container.getHostIP(), container.getName());
//			}
//		}
//	}

