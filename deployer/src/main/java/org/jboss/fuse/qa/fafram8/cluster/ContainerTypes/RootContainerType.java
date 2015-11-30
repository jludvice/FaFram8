package org.jboss.fuse.qa.fafram8.cluster.ContainerTypes;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.deployer.Deployer;
import org.jboss.fuse.qa.fafram8.deployer.LocalDeployer;
import org.jboss.fuse.qa.fafram8.deployer.RemoteDeployer;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.ssh.FuseSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class represents root container type.
 * Created by mmelko on 09/10/15.
 */
@Slf4j
public class RootContainerType extends ContainerType {

	@Setter
	@Getter
	private Deployer deployer;

	@Setter
	private String username;

	@Setter
	private String password;

	@Setter
	private int port;

	@Setter
	@Getter
	private List<String> commands = new ArrayList<>();

	@Override
	protected void initExexutor() {
		this.executor = deployer.getContainerManager().getExecutor();
	}

	/**
	 * Constructor.
	 *
	 * @param c container reference.
	 */
	public RootContainerType(Container c) {
		super(c);
	}

	private void prepare() {
		final Node node = container.getHostNode();
		final SSHClient fuseSsh = new FuseSSHClient().hostname(node.getHost()).fuseSSHPort().username(username).password(password);

		if (!container.getHostNode().getHost().contains("localhost")) {
			final SSHClient nodeSsh = new NodeSSHClient().hostname(node.getHost()).port(node.getPort()).username(node.getUsername()).password(node.getPassword());
			try {
				this.deployer = new RemoteDeployer(nodeSsh, fuseSsh);
			} catch (SSHClientException e) {
				e.printStackTrace();
			}
			log.info("Setting up remote deployment on host " + node.getHost() + ":" + node.getPort());
		} else {
			this.deployer = new LocalDeployer(fuseSsh);
		}
	}

	@Override
	public String createContainer() {
		prepare();
		initExexutor();
		deployer.getContainerManager().setCommands(commands);
		this.deployer.setup();
		return "root container is created";
	}

	@Override
	public void deleteContainer() {
		//TODO(mmelko): implement delete of container
	}

	@Override
	public void stopContainer() {
		if (deployer != null) {
			deployer.tearDown();
		}
	}

	@Override
	public void startContainer() {
		//TODO(mmelko): implement
	}

	@Override
	public String getCreateCommand() {
		return null;
	}

	/**
	 * add command into list of commands.
	 *
	 * @param command command which will be added into list.
	 */
	public void addCommand(String command) {
		this.commands.add(command);
	}
}
