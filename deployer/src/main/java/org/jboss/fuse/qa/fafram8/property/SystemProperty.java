package org.jboss.fuse.qa.fafram8.property;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * System property class.
 * Created by avano on 20.8.15.
 */
@Slf4j
public class SystemProperty {
	private static SystemProperty instance = null;

	@Getter
	private static Set<String> properties = null;

	/**
	 * Constructor.
	 */
	protected SystemProperty() {
	}

	/**
	 * Gets the instance.
	 *
	 * @return instance
	 */
	public static SystemProperty getInstance() {
		if (instance == null) {
			instance = new SystemProperty();
			properties = new HashSet<>();
		}

		return instance;
	}

	/**
	 * Sets the system property and adds it to the set.
	 *
	 * @param property property
	 * @param value value
	 */
	public static void set(String property, String value) {
		// Force the initialization
		SystemProperty.getInstance();

		// Check if such property exists - if yes do nothing
		if (System.getProperty(property) == null) {
			log.debug(String.format("Setting system property %s to value '%s'", property, value));
			System.setProperty(property, value);
			getProperties().add(property);
		}
	}

	/**
	 * Clears all set properties.
	 */
	public static void clearAllProperties() {
		// Force the initialization - for example when the unzip fails
		SystemProperty.getInstance();

		for (String prop : getProperties()) {
			System.clearProperty(prop);
			log.debug("System property " + prop + " cleared.");
		}

		// Clear all the properties at the end so that they will not stay here when executing multiple tests
		properties.clear();
	}

	/**
	 * Getter.
	 *
	 * @return remote host
	 */
	public static String getHost() {
		return System.getProperty(FaframConstant.HOST);
	}

	/**
	 * Getter.
	 *
	 * @return host ssh port
	 */
	public static int getHostPort() {
		return Integer.parseInt(System.getProperty(FaframConstant.HOST_PORT, "22"));
	}

	/**
	 * Getter.
	 *
	 * @return host login
	 */
	public static String getHostUser() {
		return System.getProperty(FaframConstant.HOST_USER, "fuse");
	}

	/**
	 * Getter.
	 *
	 * @return host password
	 */
	public static String getHostPassword() {
		return System.getProperty(FaframConstant.HOST_PASSWORD, "fuse");
	}

	/**
	 * Getter.
	 *
	 * @return fuse user
	 */
	public static String getFuseUser() {
		return System.getProperty(FaframConstant.FUSE_USER, "admin");
	}

	/**
	 * Getter.
	 *
	 * @return fuse password
	 */
	public static String getFusePassword() {
		return System.getProperty(FaframConstant.FUSE_PASSWORD, "admin");
	}

	/**
	 * Getter.
	 *
	 * @return product group id
	 */
	public static String getFuseGroup() {
		return System.getProperty(FaframConstant.FUSE_GROUP);
	}

	/**
	 * Getter.
	 *
	 * @return product artifact id
	 */
	public static String getFuseId() {
		return System.getProperty(FaframConstant.FUSE_ID);
	}

	/**
	 * Getter.
	 *
	 * @return product version
	 */
	public static String getFuseVersion() {
		return System.getProperty(FaframConstant.FUSE_VERSION);
	}

	/**
	 * Getter.
	 *
	 * @return product url
	 */
	public static String getFuseZip() {
		return System.getProperty(FaframConstant.FUSE_ZIP);
	}

	/**
	 * Getter.
	 *
	 * @return karaf start wait time
	 */
	public static int getStartWaitTime() {
		return Integer.parseInt(System.getProperty(FaframConstant.START_WAIT_TIME, "120"));
	}

	/**
	 * Getter.
	 *
	 * @return karaf stop wait time
	 */
	public static int getStopWaitTime() {
		return Integer.parseInt(System.getProperty(FaframConstant.STOP_WAIT_TIME, "30"));
	}

	/**
	 * Getter.
	 *
	 * @return fabric provision wait time
	 */
	public static int getProvisionWaitTime() {
		return Integer.parseInt(System.getProperty(FaframConstant.PROVISION_WAIT_TIME, "300"));
	}

	/**
	 * Getter.
	 *
	 * @return patch install wait time
	 */
	public static int getPatchWaitTime() {
		return Integer.parseInt(System.getProperty(FaframConstant.PATCH_WAIT_TIME, "120"));
	}

	/**
	 * Getter.
	 *
	 * @return keep folder
	 */
	public static boolean isKeepFolder() {
		return System.getProperty(FaframConstant.KEEP_FOLDER) != null;
	}

	/**
	 * Getter.
	 *
	 * @return fafram folder name
	 */
	public static String getFaframFolder() {
		return System.getProperty(FaframConstant.FAFRAM_FOLDER, "fafram");
	}

	/**
	 * Getter.
	 *
	 * @return fabric flag
	 */
	public static boolean isFabric() {
		return System.getProperty(FaframConstant.FABRIC) != null;
	}

	/**
	 * Getter.
	 *
	 * @return fabric settings
	 */
	public static String getFabric() {
		return System.getProperty(FaframConstant.FABRIC);
	}

	/**
	 * Getter.
	 *
	 * @return fuse path
	 */
	public static String getFusePath() {
		return System.getProperty(FaframConstant.FUSE_PATH);
	}

	/**
	 * Getter.
	 *
	 * @return patch
	 */
	public static String getPatch() {
		return System.getProperty(FaframConstant.PATCH);
	}

	/**
	 * Getter.
	 *
	 * @return absolute path to working directory
	 */
	public static String getWorkingDirectory() {
		return System.getProperty(FaframConstant.WORKING_DIRECTORY, "");
	}

	/**
	 * Getter.
	 *
	 * @return patch directory
	 */
	public static String getPatchDir() {
		return System.getProperty(FaframConstant.PATCH_DIR, "/home/fuse/patches");
	}

	/**
	 * Getter.
	 *
	 * @return skip default user flag
	 */
	public static boolean skipDefaultUser() {
		return System.getProperty(FaframConstant.SKIP_DEFAULT_USER) != null;
	}

	/**
	 * Getter.
	 *
	 * @return patch standalone flag
	 */
	public static boolean patchStandalone() {
		return System.getProperty(FaframConstant.PATCH_STANDALONE) != null;
	}

	/**
	 * Getter.
	 *
	 * @return skip default jvm opts flag
	 */
	public static boolean skipDefaultJvmOpts() {
		return System.getProperty(FaframConstant.SKIP_DEFAULT_JVM_OPTS) != null;
	}

	/**
	 * Getter.
	 *
	 * @return skip default jvm opts flag
	 */
	public static boolean suppressStart() {
		return System.getProperty(FaframConstant.SUPPRESS_START) != null;
	}

	/**
	 * Getter.
	 *
	 * @return archive target path
	 */
	public static String getArchiveTarget() {
		return System.getProperty(FaframConstant.ARCHIVE_TARGET, "target/archived");
	}

	/**
	 * Getter.
	 *
	 * @return archive pattern
	 */
	public static String getArchivePattern() {
		return System.getProperty(FaframConstant.ARCHIVE_PATTERN, ",");
	}

	/**
	 * Getter.
	 *
	 * @return base directory path
	 */
	public static String getBaseDir() {
		return System.getProperty(FaframConstant.BASE_DIR);
	}

	/**
	 * Getter.
	 *
	 * @return whether to keep OS nodes after test or release them
	 */
	public static boolean isKeepOsResources() {
		return Boolean.parseBoolean(System.getProperty(FaframConstant.KEEP_OS_RESOURCES, "false"));
	}

	/**
	 * Getter.
	 *
	 * @return clean property
	 */
	public static Boolean getClean() {
		return Boolean.parseBoolean(System.getProperty(FaframConstant.CLEAN, "false"));
	}

	/**
	 * Getter.
	 * @return config path
	 */
	public static String getConfigPath() {
		return System.getProperty(FaframConstant.CONFIG_PATH, "none");
	}

	/**
	 * Getter.
	 *
	 * @return broker start wait time
	 */
	public static int getBrokerStartWaitTime() {
		return Integer.parseInt(System.getProperty(FaframConstant.BROKER_START_WAIT_TIME, "60"));
	}

	/**
	 * Getter.
	 *
	 * @return jira url
	 */
	public static String getJiraURL() {
		return System.getProperty(FaframConstant.JIRA_URL, "https://issues.jboss.org");
	}
}
