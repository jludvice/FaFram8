package org.jboss.fuse.qa.fafram8.validator;

import org.apache.commons.lang3.StringUtils;

import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
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
		log.info("Validating properties...");
		validateZip();
		validateHost();
		validatePatch();
		log.info("Validation complete!");
	}

	/**
	 * Validates zip property.
	 */
	private static void validateZip() {
		validateNonNullZip();
		validateNullZip();
	}

	/**
	 * Validates null zip.
	 */
	private static void validateNullZip() {
		final String zipFile = SystemProperty.getFuseZip();

		// Validator is called after the machine is provisioned, so the host property should be set
		// If we are on remote but not specifying zip
		if (SystemProperty.getHost() != null && zipFile == null) {
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
	private static void validateNonNullZip() {
		final String zipFile = SystemProperty.getFuseZip();
		if ("".equals(zipFile)) {
			throw new ValidatorException(FaframConstant.FUSE_ZIP + " property is empty!");
		}

		// If we are on localhost and using custom zip
		if (SystemProperty.getHost() == null && (zipFile != null && zipFile.startsWith("file")) && !SystemProperty.getProvider().contains("OpenStack")) {
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

		if (host != null && !"openstack".equals(host)) {
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
