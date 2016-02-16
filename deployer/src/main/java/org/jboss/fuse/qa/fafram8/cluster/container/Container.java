package org.jboss.fuse.qa.fafram8.cluster.container;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.FuseSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract container class - contains all the shared attributes and methods among the containers.
 * Created by avano on 1.2.16.
 */
@Slf4j
@ToString
public abstract class Container {
	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String user = SystemProperty.getFuseUser();

	@Getter
	@Setter
	private String password = SystemProperty.getFusePassword();

	@Getter
	@Setter
	private Executor executor;

	@Getter
	@Setter
	private Node node;

	@Getter
	@Setter
	private Container parent;

	@Getter
	@Setter
	// Because when using .containers(Root, Child), the child is spawned before the root is in the list,
	// so we just save it's name and assign the actual container object in the create method
	private String parentName;

	@Getter
	@Setter
	private boolean root;

	@Getter
	@Setter
	private boolean online;

	@Getter
	@Setter
	private List<String> commands = new ArrayList<>();

	@Getter
	@Setter
	private List<String> bundles = new ArrayList<>();

	@Getter
	@Setter
	private List<String> profiles = new ArrayList<>();

	@Getter
	@Setter
	private boolean fabric;

	@Getter
	@Setter
	private String fabricCreateArguments = "";

	/**
	 * Creates a container.
	 */
	public abstract void create();

	/**
	 * Destoys a container.
	 */
	public abstract void destroy();

	/**
	 * Restarts a container.
	 */
	public abstract void restart();

	/**
	 * Starts a container.
	 */
	public abstract void start();

	/**
	 * Stops a container.
	 */
	public abstract void stop();

	/**
	 * Kills a container.
	 */
	public abstract void kill();

	/**
	 * Executes a command in container.
	 *
	 * @param command command to execute
	 * @return command response
	 */
	public abstract String executeCommand(String command);

	/**
	 * Creates the executor from the specified attributes. It is used in builder and in OpenstackProvisionProvider.
	 *
	 * @return executor instance
	 */
	public Executor createExecutor() {
		final SSHClient fuseClient = new FuseSSHClient()
				.host(this.getNode().getHost())
				.fuseSSHPort()
				.username(this.getUser())
				.password(this.getPassword());
		return new Executor(fuseClient);
	}

	/**
	 * Setter.
	 *
	 * @param name name
	 * @return this
	 */
	public Container name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param user user
	 * @return this
	 */
	public Container user(String user) {
		this.user = user;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param password password
	 * @return this
	 */
	public Container password(String password) {
		this.password = password;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param node node
	 * @return this
	 */
	public Container node(Node node) {
		this.node = node;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param parent parent container
	 * @return this
	 */
	public Container parent(Container parent) {
		this.parent = parent;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param parentName parent container name
	 * @return this
	 */
	public Container parentName(String parentName) {
		this.parentName = parentName;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param commands commands array
	 * @return this
	 */
	public Container commands(List<String> commands) {
		this.commands = commands;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param bundles bundles array
	 * @return this
	 */
	public Container bundles(List<String> bundles) {
		this.bundles = bundles;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param root root flag
	 * @return this
	 */
	public Container root(boolean root) {
		this.root = root;
		return this;
	}

	/**
	 * Setter.
	 *
	 * @param profiles profiles list
	 * @return this
	 */
	public Container profiles(List<String> profiles) {
		this.profiles = profiles;
		return this;
	}

	public Container fabric(boolean fabric) {
		this.fabric = fabric;
		return this;
	}

	public Container fabricCreateArguments(String fabricCreateArguments) {
		this.fabricCreateArguments = fabricCreateArguments;
		return this;
	}
}
