package org.jboss.fuse.qa.fafram8.resource;

import static org.jboss.fuse.qa.fafram8.modifier.impl.ArchiveModifier.registerArchiver;
import static org.jboss.fuse.qa.fafram8.modifier.impl.CommandHistoryModifier.saveCommandHistory;
import static org.jboss.fuse.qa.fafram8.modifier.impl.FileModifier.moveFile;
import static org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier.setDefaultJvmOpts;
import static org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier.setJvmOpts;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.extendProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.PropertyModifier.putProperty;
import static org.jboss.fuse.qa.fafram8.modifier.impl.RandomModifier.changeRandomSource;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.cluster.ContainerTypes.RootContainerType;
import org.jboss.fuse.qa.fafram8.configuration.ConfigurationParser;

import org.jboss.fuse.qa.fafram8.manager.LocalNodeManager;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.StaticProvider;

import org.jboss.fuse.qa.fafram8.validator.Validator;

import org.junit.rules.ExternalResource;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
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
		}
		else {
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

	private void initRootContainer() {
		this.rootContainer = builder.rootWithMappedProperties().name(containerName).build();
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
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
		if(rootContainer !=null)
			rootContainer.stop();
	}

	/**
	 * Sets the default modifiers common for both local and remote deploy.
	 */
	private void setDefaultModifiers() {
		if (!SystemProperty.skipDefaultUser()) {
			// Add default user
			ModifierExecutor.addModifiers(putProperty("etc/users.properties", SystemProperty.getFuseUser(),
					SystemProperty.getFusePassword() + ",admin,manager,viewer,Monitor, Operator, Maintainer, "
							+ "Deployer, Auditor, Administrator, SuperUser"));
		}

		if (!SystemProperty.skipDefaultJvmOpts()) {
			ModifierExecutor.addModifiers(setDefaultJvmOpts());
		}

		ModifierExecutor.addModifiers(changeRandomSource());
		ModifierExecutor.addPostModifiers(saveCommandHistory());
		ModifierExecutor.addPostModifiers(registerArchiver());
	}
//
//	/**
//	 * Sets up the local deployment.
	//	 */
//	private void setupLocalDeployment() {
//		final int defaultPort = 8101;
//
//		// Create a local deployer with local SSH Client and assign to deployer variable
//		deployer = new LocalDeployer(new FuseSSHClient().hostname("localhost").port(defaultPort).username(SystemProperty
//				.getFuseUser()).password(SystemProperty.getFusePassword()));
//	}
//
//	/**
//	 * Sets up the remote deployment.
//	 */
//	private void setupRemoteDeployment() throws SSHClientException {
//		final SSHClient node = new NodeSSHClient().hostname(SystemProperty.getHost()).port(SystemProperty.getHostPort())
//				.username(SystemProperty.getHostUser()).password(SystemProperty.getHostPassword());
//
//		final SSHClient fuse = new FuseSSHClient().hostname(SystemProperty.getHost()).fuseSSHPort().username(
//				SystemProperty.getFuseUser()).password(SystemProperty.getFusePassword());
//
//		deployer = new RemoteDeployer(node, fuse);
//
//
//	}

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
		this.rootContainer.waitForProvision();
	}

	/**
	 * Waits until the defined standalone patch status.
	 *
	 * @param patchName patch name
	 * @param status patch status (true/false)
	 */
	public void waitForPatch(String patchName, boolean status) {
		((RootContainerType) this.rootContainer.getContainerType()).getDeployer().getContainerManager().getExecutor().waitForPatchStatus(patchName, status);
	}

	/**
	 * Restarts the container.
	 */
	public void restart() {
		// TODO(avano): probably won't be needed on remote
		((LocalNodeManager) ((RootContainerType) rootContainer.getContainerType()).getDeployer().getNodeManager()).restart();
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
	}

	/**
	 * Set ProvisionProvider implementation to Fafram.
	 *
	 * @param provider Implementation of ProvisionProvider interface
	 * @return this
	 */
	public Fafram provideNodes(ProvisionProvider provider) {
		provisionProvider = provider;
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
				if (c.isEnssemble()) {
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
		//TODO(avano): set real name to root container.
		return this;
	}

	/**
	 * Adds command into list of commands which should be executed right after fabric create / at the end of initialization.
	 *
	 * @param commands list of commands
	 * @return this
	 */
	public Fafram command(String... commands) {
		for (String s : commands) {
			this.commands.add(s);
		}
		return this;
	}
}
