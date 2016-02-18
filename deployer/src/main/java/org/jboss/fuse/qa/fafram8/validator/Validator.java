package org.jboss.fuse.qa.fafram8.validator;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.patcher.Patcher;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;
import java.net.InetAddress;

import lombok.extern.slf4j.Slf4j;

/**
 * Properties validator class. Validates if specified property combinations are valid with given deployment mode.
 * Created by avano on 21.9.15.
 */
@Slf4j
public final class Validator {
	/**
	 * Private constructor.
	 */
	private Validator() {
	}

	/**
	 * Validate method.
	 */
	public static void validate() {
		log.info("Validating containers...");
		validateContainers();
		log.info("Validation complete!");
	}

	/**
	 * Validates containers in container list.
	 */
	private static void validateContainers() {
		if (ContainerManager.getContainerList().isEmpty()) {
			validateDefaultContainer();
			return;
		}

		for (Container c : ContainerManager.getContainerList()) {
			if (c instanceof RootContainer) {
				validateRootContainer(c);
			} else if (c instanceof ChildContainer) {
				validateChildContainer(c);
			} else {
				validateSshContainer(c);
			}
		}
	}

	/**
	 * Validates root container.
	 *
	 * @param c root container
	 */
	private static void validateRootContainer(Container c) {
		if (c.getNode() == null) {
			throw new ValidatorException("Root container (" + c.getName() + ") node is null!");
		}

		validateZip(c.getNode().getHost());

		if ("".equals(c.getName())) {
			throw new ValidatorException("Root name can't be empty!");
		}

		if (c.getNode().getHost() == null || c.getNode().getPort() == 0 || c.getNode().getUsername() == null
				|| c.getNode().getPassword() == null) {
			throw new ValidatorException("Atleast one of root container node attributes (" + c.getName() + ") is not set!");
		}

		if (c.getParent() != null || c.getParentName() != null) {
			throw new ValidatorException("Root container (" + c.getName() + ") can't have a parent! (Parent container"
					+ " or parent container name was set)");
		}
	}

	/**
	 * Validates child container.
	 *
	 * @param c child container
	 */
	private static void validateChildContainer(Container c) {
		if ("".equals(c.getName())) {
			throw new ValidatorException("Child name can't be empty!");
		}

		if (c.getParentName() == null && c.getParent() == null) {
			throw new ValidatorException("Child container (" + c.getName() + ") must have a parent!");
		}

		if (c.getParentName() != null) {
			if (ContainerManager.getContainer(c.getParentName()) == null) {
				throw new ValidatorException(String.format("Parent of %s (%s) does not exist in container list!", c.getName(), c.getParentName()));
			}
		}
	}

	/**
	 * Validates ssh container.
	 *
	 * @param c container
	 */
	private static void validateSshContainer(Container c) {
		if ("".equals(c.getName())) {
			throw new ValidatorException("SSH container name can't be empty!");
		}

		if (c.getNode() == null) {
			throw new ValidatorException("SSH container node is null!");
		}

		if (c.getNode().getHost() == null || c.getNode().getUsername() == null || c.getNode().getPassword() == null) {
			throw new ValidatorException("Atleast one of ssh container (" + c.getName() + ") node attributes is not set!");
		}

		if (c.getParentName() == null && c.getParent() == null) {
			throw new ValidatorException("SSH container (" + c.getName() + ") must have a parent!");
		}

		if (c.getParentName() != null) {
			if (ContainerManager.getContainer(c.getParentName()) == null) {
				throw new ValidatorException(String.format("Parent of %s (%s) does not exist in container list!", c.getName(), c.getParentName()));
			}
		}
	}

	/**
	 * Validates default container built from system properties.
	 */
	private static void validateDefaultContainer() {
		if ("localhost".equals(SystemProperty.getHost())) {
			validateZip(SystemProperty.getHost());
			validatePatch();
		} else {
			validateHost();
		}
	}

	/**
	 * Validates zip property.
	 */
	private static void validateZip(String host) {
		validateNonNullZip(host);
		validateNullZip(host);
	}

	/**
	 * Validates null zip.
	 */
	private static void validateNullZip(String host) {
		final String zipFile = SystemProperty.getFuseZip();

		// Validator is called after the machine is provisioned, so the host property should be set
		// If we are on remote but not specifying zip
		if (!"localhost".equals(host) && zipFile == null) {
			throw new ValidatorException(FaframConstant.FUSE_ZIP + " property is not set on remote!");
		}

		// If the maven properties are not set and not using custom zip
		if (zipFile == null && (SystemProperty.getFuseId() == null || SystemProperty.getFuseGroup() == null || SystemProperty.getFuseVersion()
				== null)) {
			throw new ValidatorException("Maven properties and zip file properties are not set!");
		}
	}

	/**
	 * Validates non null zip.
	 */
	private static void validateNonNullZip(String host) {
		final String zipFile = SystemProperty.getFuseZip();
		if ("".equals(zipFile)) {
			throw new ValidatorException(FaframConstant.FUSE_ZIP + " property is empty!");
		}

		// If we are on localhost and using custom zip
		if ("localhost".equals(host) && (zipFile != null && zipFile.startsWith("file")) && !SystemProperty.getProvider()
				.contains("OpenStack")) {
			if (!new File(StringUtils.substringAfter(zipFile, "file://")).exists()) {
				throw new ValidatorException(String.format("Specified file (%s) does not exist!", zipFile));
			}
		}
	}

	/**
	 * Validates host property.
	 */
	private static void validateHost() {
		final int timeout = 30000;
		final String host = SystemProperty.getHost();

		if ("".equals(host)) {
			throw new ValidatorException(FaframConstant.HOST + " property is empty!");
		}

		if (!"localhost".equals(host) && !"openstack".equals(host)) {
			try {
				final InetAddress inet = InetAddress.getByName(host);
				if (!inet.isReachable(timeout)) {
					throw new ValidatorException(String.format("Specified host (%s) is not reachable!", host));
				}
			} catch (Exception ex) {
				throw new ValidatorException("Unknown host " + host + "!");
			}
		}
	}

	/**
	 * Validates patch property.
	 */
	private static void validatePatch() {
		final String patch = SystemProperty.getPatch();

		// Do nothing if we don't have a patch
		if (patch == null || "".equals(patch)) {
			return;
		}

		// If we get patch by name substring
		if (!StringUtils.startsWithAny(patch, "http", "file", "scp")) {
			if (Patcher.getPatches().length == 0) {
				throw new ValidatorException(String.format("Specified patch (%s) not found", patch));
			}
		}

		if (patch.startsWith("file")) {
			if (!new File(StringUtils.substringAfter(patch, "file://")).exists()) {
				throw new ValidatorException(String.format("Specified file (%s) does not exist!", patch));
			}
		}
	}
}
