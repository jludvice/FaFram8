package org.jboss.fuse.qa.fafram8.resource;

import static org.jboss.fuse.qa.fafram8.modifier.impl.AccessRightsModifier.setExecutable;
import static org.jboss.fuse.qa.fafram8.modifier.impl.ArchiveModifier.registerArchiver;
import static org.jboss.fuse.qa.fafram8.modifier.impl.CommandHistoryModifier.saveCommandHistory;
import static org.jboss.fuse.qa.fafram8.modifier.impl.FileModifier.moveFile;
import static org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier.setDefaultJvmOpts;
import static org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier.setJvmOpts;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.extendProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.putProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RandomModifier.changeRandomSource;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RootNamesModifier.setRootNames;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;
import org.jboss.fuse.qa.fafram8.configuration.ConfigurationParser;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.StaticProvider;
import org.jboss.fuse.qa.fafram8.validator.Validator;

import org.junit.rules.ExternalResource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Fafram resource class.
 * Created by avano on 19.8.15.
 */
@Slf4j
public class Fafram extends ExternalResource {
	//List of containers used in test
	@Getter
	private final List<Container> containerList = new LinkedList<>();

	//Provision provider instance in case of remote deployment
	@Getter
	private static ProvisionProvider provisionProvider = new StaticProvider();

	@Setter
	private List<String> commands = new LinkedList<>();

	@Setter
	private List<String> bundles = new LinkedList<>();

	@SuppressWarnings("FieldCanBeLocal")
	private ConfigurationParser parser;

	@Getter
	private ContainerBuilder builder = new ContainerBuilder();

	@Getter
	private Container rootContainer;

	private String containerName = "root";

	/**
	 * Constructor.
	 */
	public Fafram() {
		builder.setFafram(this);
	}

	/**
	 * Create Fafram and set ProvisionProvider implementation for remote deployment on-demmand provisioning.
	 *
	 * @param provisionProvider implementation of ProvisionProvider interface.
	 */
	public Fafram(ProvisionProvider provisionProvider) {
		this.provisionProvider = provisionProvider;
	}

	/**
	 * Adds container object model to container list.
	 *
	 * @param container container specification picked up from configuration file
	 */
	public void addContainer(Container container) {
		containerList.add(container);
	}

	@Override
	protected void before() {
		setup();
	}

	@Override
	protected void after() {
		tearDown();
	}

	/**
	 * Start method.
	 *
	 * @return this
	 */
	public Fafram setup() {
		initConfiguration();

		if (SystemProperty.getHost() == null) {
			log.info("Setting up local deployment");
			Validator.validate();
			SystemProperty.set(FaframConstant.HOST, "localhost");
		} else {
			Validator.validate();
		}
		printLogo();
		setDefaultModifiers();
		//prepare container list

		initRootContainer();
		prepareNodes(provisionProvider);
		initContainers();
		return this;
	}

	/**
	 * Init of the root container.
	 */
	private void initRootContainer() {
		this.rootContainer = builder.rootWithMappedProperties().name(containerName).build();
		if (bundles != null && !bundles.isEmpty()) {
			((RootContainerType) rootContainer.getContainerType()).setBundles(bundles);
		}

		if (commands != null && !commands.isEmpty()) {
			((RootContainerType) rootContainer.getContainerType()).setCommands(commands);
		}
		containerList.add(0, rootContainer);
	}

	/**
	 * Configuration init.
	 */
	public void initConfiguration() {
		if (!SystemProperty.getConfigPath().contains("none")) {
			this.parser = new ConfigurationParser(SystemProperty.getConfigPath());
			this.parser.setContainerBuilder(this.builder);

			try {
				parser.parseConfigurationFile();
			} catch (Exception e) {
				throw new FaframException("Problem with configuration file.");
			}
		}
	}

	/**
	 * Prints the logo. Feel free to change.
	 */
	public void printLogo() {
		log.info("\n  ___       ___                  _____  \n"
				+ " / __)     / __)                / ___ \\ \n"
				+ "| |__ ____| |__ ____ ____ ____ ( (   ) )\n"
				+ "|  __) _  |  __) ___) _  |    \\ > > < < \n"
				+ "| | ( ( | | | | |  ( ( | | | | ( (___) )\n"
				+ "|_|  \\_||_|_| |_|   \\_||_|_|_|_|\\_____/ \n\n");
	}

	/**
	 * Stop method.
	 */
	public void tearDown() {
		// Do nothing if deployer is null - when the validation fails.
		//TODO(mmelko): cleanup the containers node .. here is the right place

		for (Container c : containerList) {
			if (!(c.getContainerType() instanceof RootContainerType)) {
				log.debug("Deleting " + c.getName());
				c.delete();
			}
		}

		if (rootContainer != null) {
			log.debug("Deleting " + rootContainer.getName());
			rootContainer.stop();
		}
	}

	/**
	 * Sets the default modifiers common for both local and remote deploy.
	 */
	private void setDefaultModifiers() {
		if (!SystemProperty.skipDefaultUser()) {
			// Add default user which is now fafram/fafram with only role Administrator for more transparent tests
			ModifierExecutor.addModifiers(putProperty("etc/users.properties", SystemProperty.getFuseUser(),
					SystemProperty.getFusePassword() + ",Administrator"));
		}

		if (!SystemProperty.skipDefaultJvmOpts()) {
			ModifierExecutor.addModifiers(setDefaultJvmOpts());
		}

		ModifierExecutor.addModifiers(
				setExecutable("bin/karaf", "bin/start", "bin/stop"),
				setRootNames(),
				changeRandomSource()
		);

		ModifierExecutor.addPostModifiers(
				saveCommandHistory(),
				registerArchiver()
		);
	}

	/**
	 * Executes a command in node shell.
	 *
	 * @param command command
	 * @return command response
	 */
	public String executeNodeCommand(String command) {
		return ((RootContainerType) rootContainer.getContainerType()).getDeployer().getNodeManager().getExecutor().executeCommand(command);
	}

	/**
	 * Executes a command in root container shell.
	 *
	 * @param command command to execute on root container
	 * @return command response
	 */
	public String executeCommand(String command) {
		return rootContainer.getContainerType().getExecutor().executeCommand(command);
	}

	/**
	 * Adds a new user.
	 *
	 * @param user user
	 * @param password password
	 * @param roles comma-separated roles
	 * @return this
	 */
	public Fafram addUser(String user, String password, String roles) {
		ModifierExecutor.addModifiers(putProperty("etc/users.properties", user, password + "," + roles));
		return this;
	}

	/**
	 * Modifies (add/extend) a property.
	 *
	 * @param file file path relative to karaf home
	 * @param key key
	 * @param value value
	 * @param extend extend flag
	 * @return this
	 */
	public Fafram modifyProperty(String file, String key, String value, boolean extend) {
		if (extend) {
			ModifierExecutor.addModifiers(putProperty(file, key, value));
		} else {
			ModifierExecutor.addModifiers(extendProperty(file, key, value));
		}

		return this;
	}

	/**
	 * Replaces a file.
	 *
	 * @param fileToReplace file to replace
	 * @param fileToUse file to use
	 * @return this
	 */
	public Fafram replaceFile(String fileToReplace, String fileToUse) {
		ModifierExecutor.addModifiers(moveFile(fileToReplace, fileToUse));
		return this;
	}

	/**
	 * Provides deployment with Fabric environment.
	 *
	 * @return this
	 */
	public Fafram withFabric() {
		return withFabric("");
	}

	/**
	 * Provide deployment with Fabric provision.
	 *
	 * @param opts fabric create options
	 * @return this
	 */
	public Fafram withFabric(String opts) {
		SystemProperty.set(FaframConstant.FABRIC, opts);
		return this;
	}

	/**
	 * Suppress the default user add.
	 *
	 * @return this
	 */
	public Fafram withoutDefaultUser() {
		SystemProperty.set(FaframConstant.SKIP_DEFAULT_USER, "");
		return this;
	}

	/**
	 * Patches standalone container. Only useful together with withFabric().
	 *
	 * @return this
	 */
	public Fafram patchStandalone() {
		SystemProperty.set(FaframConstant.PATCH_STANDALONE, "");
		return this;
	}

	/**
	 * Sets the JVM options.
	 *
	 * @param xms xms
	 * @param xmx xmx
	 * @param permMem perm mem
	 * @param maxPermMem max perm mem
	 * @return this
	 */
	public Fafram setJvmOptions(String xms, String xmx, String permMem, String maxPermMem) {
		ModifierExecutor.addModifiers(setJvmOpts(xms, xmx, permMem, maxPermMem));
		SystemProperty.set(FaframConstant.SKIP_DEFAULT_JVM_OPTS, "");
		return this;
	}

	/**
	 * Suppresses the start of the fuse - probably testing purposes only.
	 *
	 * @return this
	 */
	public Fafram suppressStart() {
		SystemProperty.set(FaframConstant.SUPPRESS_START, "");
		return this;
	}

	/**
	 * Archive files pattern setter.
	 *
	 * @param pattern pattern
	 * @return this
	 */
	public Fafram archive(String pattern) {
		SystemProperty.set(FaframConstant.ARCHIVE_PATTERN, pattern);
		return this;
	}

	/**
	 * Keep folder flag setter.
	 *
	 * @return this
	 */
	public Fafram keepFolder() {
		SystemProperty.set(FaframConstant.KEEP_FOLDER, "");
		return this;
	}

	/**
	 * Skips waiting for broker.
	 * @return this
	 */
	public Fafram skipBrokerWait() {
		SystemProperty.set(FaframConstant.SKIP_BROKER_WAIT, "true");
		return this;
	}

	/**
	 * Gets the full product path.
	 *
	 * @return product path
	 */
	public String getProductPath() {
		return SystemProperty.getFusePath();
	}

	/**
	 * Waits for the container to provision.
	 *
	 * @param containerName container name
	 */
	public void waitForProvisioning(String containerName) {
		this.rootContainer.waitForProvision(containerName);
	}

	/**
	 * Waits until the defined standalone patch status.
	 *
	 * @param patchName patch name
	 * @param status patch status (true/false)
	 */
	public void waitForPatch(String patchName, boolean status) {
		((RootContainerType) this.rootContainer.getContainerType()).getDeployer().getContainerManager().getExecutor().waitForPatchStatus(patchName,
				status);
	}

	/**
	 * Restarts the container.
	 */
	public void restart() {
		((RootContainerType) rootContainer.getContainerType()).getDeployer().getNodeManager().restart();
	}

	/**
	 * Triggers specified provision provider. It will call providers functionality to provision pool of nodes required
	 * to satisfy needs of test deployment. Provider will create node for every container listed in Fafram
	 * .containerList.
	 * When the container is marked as root provider will assign public IP to it. Otherwise local IP will be provided.
	 * Provider have to implement ProvisionProvider interface.
	 *
	 * @param provider provider type name
	 */
	public void prepareNodes(ProvisionProvider provider) {
		provider.createServerPool(containerList);
		provider.assignAddresses(containerList);

		// TODO(avano): Here is problem with timeout after spawning openstack nodes. Some timeout is needed because login module is not started ->
		// problem with iptables
		// TODO(rjakubco): For now load iptables(kill internet) here. All nodes should be already spawned and it makes sense to create the proper
		// environment
		provider.loadIPTables(containerList);
	}

	/**
	 * Set ProvisionProvider implementation to Fafram.
	 *
	 * @param provider Implementation of ProvisionProvider interface
	 * @return this
	 */
	public Fafram provideNodes(ProvisionProvider provider) {
		provisionProvider = provider;
		SystemProperty.set(FaframConstant.PROVIDER, provider.getClass().getName());
		return this;
	}

	/**
	 * Defines hostname/IP address for remote running.
	 *
	 * @param host hostname or ip to remote host for running Fuse
	 * @return this
	 */
	public Fafram host(String host) {
		SystemProperty.set(FaframConstant.HOST, host);
		return this;
	}

	/**
	 * Defines host username.
	 *
	 * @param username username for remote host machine
	 * @return this
	 */
	public Fafram hostUser(String username) {
		SystemProperty.set(FaframConstant.HOST_USER, username);
		return this;
	}

	/**
	 * Defines host password.
	 *
	 * @param password password for remote machine
	 * @return this
	 */
	public Fafram hostPassword(String password) {
		SystemProperty.set(FaframConstant.HOST_PASSWORD, password);
		return this;
	}

	/**
	 * URL to zip with fuse.
	 *
	 * @param zip url
	 * @return this
	 */
	public Fafram fuseZip(String zip) {
		SystemProperty.set(FaframConstant.FUSE_ZIP, zip);
		return this;
	}

	/**
	 * Defines that remote Fuse and its cluster shouldn't be deleted and Fafram should only connect to existing Fuse.
	 *
	 * @return this
	 */
	public Fafram onlyConnect() {
		SystemProperty.set(FaframConstant.CLEAN, "false");

		return this;
	}

	/**
	 * Defines path to configuration file.
	 *
	 * @param configPath patch to configuration file
	 * @return this
	 */
	public Fafram setConfigPath(String configPath) {
		SystemProperty.set(FaframConstant.CONFIG_PATH, configPath);
		return this;
	}

	/**
	 * Container initialization. If container is already live just skip it.
	 */
	//TODO(ecervena): implement parallel container spawn
	public void initContainers() {
		try {
			//TODO(ecervena): implement waiting for
			final int timeout = 15000;
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Container c : containerList) {
			if (!(c.getContainerType() instanceof RootContainerType) && c.getParentContainer() == null) {
				c.setParentContainer(this.rootContainer);
			}
		}

		String ensembleServers = "";

		for (Container c : containerList) {
			if (!c.isLive()) {
				c.create();
				if (c.isEnsemble()) {
					ensembleServers += " " + c.getName();
				}
			} else {
				log.debug("Container " + c.getName() + " is already running");
			}
		}

		if (!"".equals(ensembleServers)) {
			executeCommand("ensemble-add --force " + ensembleServers);
			this.rootContainer.waitForProvision();
		}
	}

	/**
	 * Specifies name of the root container.
	 *
	 * @param name root containers name.
	 * @return this
	 */
	public Fafram name(String name) {
		this.containerName = name;

		return this;
	}

	/**
	 * Adds command into list of commands which should be executed right after fabric create / at the end of initialization.
	 *
	 * @param commands list of commands
	 * @return this
	 */
	public Fafram command(String... commands) {
		this.commands.addAll(Arrays.asList(commands));
		return this;
	}

	/**
	 * Turns environment to offline mode. For this purpose the "iptables-no-internet" configuration file is used which
	 * should be located in specified user's home folder on all nodes. This file is loaded into the iptables on all nodes
	 * specified in FaFram.
	 * <p/>
	 * This method can be used only with ecervena snapshots on OpenStack or snapshots that contain mentioned folder.
	 * <p/>
	 * By default the snapshot used for spawning containers using the OpenStack provider contains this file.
	 *
	 * @return this
	 */
	public Fafram offline() {
		SystemProperty.set(FaframConstant.OFFLINE, "true");
		return this;
	}

	/**
	 * Loads up a custom iptables configuration file from local host and uploads it to remote hosts and executes them.
	 * This method sets up the environment to environment defined in provided the configuration file.
	 * <p/>
	 * This option should be only used on OpenStack machines with sudo command configured to be used without password
	 * otherwise it won't work. Also the snapshots should have exchanged ssh key to be able to connect between them
	 * without password.
	 * <p/>
	 * At the moment this method is only experimental and should be used with caution. Cleaning up the iptables after
	 * the test is not completed yet.
	 *
	 * @param localFilePath path to iptables configuration file
	 * @return this
	 */
	public Fafram loadIPtablesConfigurationFile(String localFilePath) {
		SystemProperty.set(FaframConstant.IPTABLES_CONF_FILE_PATH, localFilePath);
		return this;
	}

	/**
	 * Return container according specified container name.
	 *
	 * @param containerName name of container
	 * @return Container object
	 */
	public Container getContainer(String containerName) {
		for (Container c : containerList) {
			if (c.getName().equals(containerName)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Add bundle into list of bundles which should be uploaded into fabric maven proxy on the end of initialization.
	 *
	 * @param bundles list of bundles
	 * @return this
	 */
	public Fafram bundle(String... bundles) {
		this.bundles.addAll(Arrays.asList(bundles));
		return this;
	}

	/**
	 * Kills container by given name.
	 *
	 * @param containerName name of the container to be killed
	 */
	public void killContainer(String containerName) {
		if ("root".equals(containerName)) {
			this.rootContainer.killContainer();
		} else {
			getContainer(containerName).killContainer();
		}
	}
}
