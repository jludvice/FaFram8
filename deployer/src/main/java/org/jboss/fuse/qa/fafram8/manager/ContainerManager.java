package org.jboss.fuse.qa.fafram8.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.exception.BundleUploadException;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.invoker.MavenPomInvoker;
import org.jboss.fuse.qa.fafram8.patcher.Patcher;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Container manager class. This class is responsible for all actions related to containers - setting up fabric,
 * patching, etc.
 * <p/>
 * Created by avano on 2.9.15.
 */
@Slf4j
public class ContainerManager {
	// Singleton instance
	private static ContainerManager instance = null;

	// List of all containers
	private static List<Container> containerList = null;

	// List of bundles that will be installed into the _default_ root container only.
	private static List<String> bundles = null;

	// List of bundles that will be executed on the _default_ root container only.
	private static List<String> commands = null;

	/**
	 * Constructor.
	 */
	protected ContainerManager() {
	}

	/**
	 * Gets the singleton instance.
	 *
	 * @return singleton instance
	 */
	public static ContainerManager getInstance() {
		if (instance == null) {
			instance = new ContainerManager();
			containerList = new ArrayList<>();
			bundles = new ArrayList<>();
			commands = new ArrayList<>();
		}

		return instance;
	}

	/**
	 * Gets the bundles list.
	 *
	 * @return bundles list
	 */
	public static List<String> getBundles() {
		// Force the initialization
		getInstance();
		return bundles;
	}

	/**
	 * Gets the command list.
	 *
	 * @return command list
	 */
	public static List<String> getCommands() {
		// Force the initialization
		getInstance();
		return commands;
	}

	/**
	 * Gets the container list.
	 *
	 * @return container list
	 */
	public static List<Container> getContainerList() {
		// Force the initialization
		getInstance();
		return containerList;
	}

	/**
	 * Gets the container by its name.
	 *
	 * @param name container name
	 * @return container instance
	 */
	public static Container getContainer(String name) {
		for (Container c : containerList) {
			if (name.equals(c.getName())) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Clears all the lists in this class - containerList, bundles, commands.
	 */
	public static void clearAllLists() {
		// Force the initialization
		getInstance();
		for (int i = containerList.size() - 1; i >= 0; i--) {
			containerList.remove(i);
		}
		for (int i = bundles.size() - 1; i >= 0; i--) {
			bundles.remove(i);
		}
		for (int i = commands.size() - 1; i >= 0; i--) {
			commands.remove(i);
		}

		log.debug("Container manager lists cleared");
	}

	/**
	 * Sets up fabric on specified container.
	 *
	 * @param c container
	 */
	public static void setupFabric(Container c) {
		if (!c.isFabric()) {
			return;
		}

		final String fabricArguments = c.getFabricCreateArguments();
		// Construct the fabric create arguments from fabric property and profiles
		String fabricString = c.getFabricCreateArguments();

		for (String profile : c.getProfiles()) {
			fabricString += " --profile " + profile;
		}

		c.executeCommand("fabric:create" + (fabricString.startsWith(" ") ? StringUtils.EMPTY : " ") + fabricString);

		try {
			c.getExecutor().waitForProvisioning(c);
		} catch (FaframException ex) {
			// Container is not provisioned in time
			throw new FaframException("Container " + c.getName() + " did not provision in time");
		}
		uploadBundles(c);
		executeStartupCommands(c);
	}

	/**
	 * Executes defined commands right after fabric-create.
	 *
	 * @param c container to execute on
	 */
	public static void executeStartupCommands(Container c) {
		if (c.getCommands() != null && !c.getCommands().isEmpty()) {
			for (String command : c.getCommands()) {
				c.executeCommand(command);
			}
		}
		c.waitForProvisioning();
	}

	/**
	 * Uploads bundles to fabric maven proxy on root container (remote).
	 *
	 * @param c container to execute on
	 */
	public static void uploadBundles(Container c) {
		if (c.getBundles() != null && !c.getBundles().isEmpty()) {
			for (String bundle : c.getBundles()) {
				final MavenPomInvoker bundleInstaller = new MavenPomInvoker(bundle,
						"http://" + c.getUser() + ":" + c.getPassword() + "@" + c.getNode().getHost() + ":8181/maven/upload");
				try {
					bundleInstaller.installFile();
				} catch (URISyntaxException | MavenInvocationException e) {
					throw new BundleUploadException(e);
				}
			}
		}
	}

	/**
	 * Patches fuse based on its mode (standalone / fabric).
	 *
	 * @param c Container instance
	 */
	public static void patchFuse(Container c) {
		if (SystemProperty.getPatch() != null) {
			if (SystemProperty.isFabric()) {
				patchFabric(c);
			} else {
				patchStandalone(c);
			}
		}
	}

	/**
	 * Patches the standalone before fabric creation.
	 *
	 * @param c container to execute on
	 */
	public static void patchStandaloneBeforeFabric(Container c) {
		if (SystemProperty.patchStandalone()) {
			patchStandalone(c);
		}
	}

	/**
	 * Patches the standalone container.
	 *
	 * @param c container to execute on
	 */
	private static void patchStandalone(Container c) {
		for (String s : Patcher.getPatches()) {
			final String patchName = getPatchName(c.executeCommand("patch:add " + s));
			c.executeCommand("patch:install " + patchName);
			c.getExecutor().waitForPatchStatus(patchName, true);
		}
	}

	/**
	 * Patches fabric root and sets the default version to the patched version.
	 *
	 * @param c Container instance
	 */
	private static void patchFabric(Container c) {
		// Create a new version
		final String version = c.executeCommand("version-create").split(" ")[2];

		// We need to check if the are using old or new patching mechanism
		if (StringUtils.containsAny(SystemProperty.getFuseVersion(), "6.1", "6.2.redhat")) {
			for (String s : Patcher.getPatches()) {
				c.executeCommand("patch-apply -u " + SystemProperty.getFuseUser() + " -p "
						+ SystemProperty.getFusePassword() + " --version " + version + " " + s);
			}
		} else {
			// 6.2.1 onwards
			for (String s : Patcher.getPatches()) {
				final String patchName = getPatchName(c.executeCommand("patch:add " + s));
				c.executeCommand("patch:fabric-install -u " + SystemProperty.getFuseUser() + " -p "
						+ SystemProperty.getFusePassword() + " --upload --version " + version + " " + patchName);
			}
		}

		c.executeCommand("container-upgrade " + version + " " + c.getName());
		c.getExecutor().waitForProvisioning(c);
		c.executeCommand("version-set-default " + version);
	}

	/**
	 * Gets the patch name from the patch-add response.
	 *
	 * @param patchAddResponse patch-add command response.
	 * @return patch name
	 */
	private static String getPatchName(String patchAddResponse) {
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

