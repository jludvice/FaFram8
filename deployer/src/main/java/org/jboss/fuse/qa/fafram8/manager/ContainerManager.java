package org.jboss.fuse.qa.fafram8.manager;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.patcher.Patcher;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Container manager class.
 * Created by avano on 2.9.15.
 */
@Slf4j
public class ContainerManager {

	@Getter
	private Executor executor;

	@Setter
	@Getter
	private List<String> commands = new ArrayList<String>();

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
		executeStartupCommands();
	}

	/**
	 * Executes defined commands right after fabric-create.
	 */
	public void executeStartupCommands() {
		if (commands != null && !commands.isEmpty()) {
			for (String command : commands) {
				executor.executeCommand(command);
			}
		}
	}

	/**
	 * Sets up fabric on specific container.
	 *
	 * @param c container on which fabric will be set.
	 */
	public void setupFabric(Container c) {
		final Executor rootExecutor = c.getContainerType().getExecutor();
		rootExecutor.executeCommand("fabric:create " + SystemProperty.getFabric());
		try {
			rootExecutor.waitForProvisioning("root");
		} catch (FaframException ex) {
			// Container is not provisioned in time
			throw new FaframException("Container did not provision in time");
		}
		executeStartupCommands();
	}

	/**
	 * Patch fuse.
	 *
	 * @param nm NodeManager instance - in case when the restart is necessary
	 */
	public void patchFuse(NodeManager nm) {
		if (SystemProperty.getPatch() != null) {
			if (SystemProperty.isFabric()) {
				patchFabric(nm);
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
	 *
	 * @param nm NodeManager instance
	 */
	private void patchFabric(NodeManager nm) {
		// Create a new version
		final String version = executor.executeCommand("version-create").split(" ")[2];

		// We need to check if the are using old or new patching mechanism
		if (StringUtils.containsAny(SystemProperty.getFuseVersion(), "6.1", "6.2.redhat")) {
			for (String s : Patcher.getPatches()) {
				executor.executeCommand("patch-apply -u " + SystemProperty.getFuseUser() + " -p " + SystemProperty
						.getFusePassword() + " --version " + version + " " + s);
			}
		} else {
			// 6.2.1 onwards
			for (String s : Patcher.getPatches()) {
				final String patchName = getPatchName(executor.executeCommand("patch:add " + s));
				executor.executeCommand("patch:fabric-install -u " + SystemProperty.getFuseUser() + " -p " + SystemProperty
						.getFusePassword() + " --upload --version " + version + " " + patchName);
			}
		}

		executor.executeCommand("container-upgrade " + version + " root");
		executor.waitForProvisioning("root", nm);
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
}

