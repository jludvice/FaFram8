package org.jboss.fuse.qa.fafram8.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.exception.BundleUploadException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.invoker.MavenPomInvoker;
import org.jboss.fuse.qa.fafram8.patcher.Patcher;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Container manager class. This class is responsible for all actions related to containers - setting up fabric,
 * patching, etc.
 *
 * Created by avano on 2.9.15.
 */
@Slf4j
public class ContainerManager {

	@Getter
	private Executor executor;

	@Setter
	@Getter
	private List<String> commands = new LinkedList<>();

	@Setter
	@Getter
	private List<String> bundles = new LinkedList<>();

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
	 *
	 * @param nm nodemanager instance in case the restart is required
	 */
	public void setupFabric(NodeManager nm) {
		executor.executeCommand("fabric:create " + SystemProperty.getFabric());
		try {
			executor.waitForProvisioning("root", nm);
		} catch (FaframException ex) {
			// Container is not provisioned in time
			throw new FaframException("Container did not provision in time");
		}
		uploadBundles();
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

	public void uploadBundles(){
		if(bundles != null && !bundles.isEmpty()){
			for(String bundle : bundles){
				MavenPomInvoker bundleInstaller = new MavenPomInvoker(bundle, "http://" + SystemProperty.getFuseUser() +
						":" + SystemProperty.getFusePassword() +"@" + SystemProperty.getHost() + ":8181/maven/upload");
				try {
					bundleInstaller.installFile();
				} catch (URISyntaxException | MavenInvocationException e) {
					throw new BundleUploadException(e);
				}
			}
		}
	}

	/**
	 * TODO(mmelko): just idea -> check if needed
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
		uploadBundles();
		executeStartupCommands();
	}

	/**
	 * Patches fuse based on its mode (standalone / fabric).
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
	 * Patches the standalone container.
	 */
	private void patchStandalone() {
		for (String s : Patcher.getPatches()) {
			final String patchName = getPatchName(executor.executeCommand("patch:add " + s));
			executor.executeCommand("patch:install " + patchName);
			executor.waitForPatchStatus(patchName, true);
		}
	}

	/**
	 * Patches fabric root and sets the default version to the patched version.
	 *
	 * @param nm NodeManager instance in case if the restart is necessary
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

