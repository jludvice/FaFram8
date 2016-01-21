package org.jboss.fuse.qa.fafram8.manager;

import org.jboss.fuse.qa.fafram8.downloader.Downloader;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.ModifierExecutor;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.io.File;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Remote node manager class.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
public class RemoteNodeManager implements NodeManager {

	// File separator
	private static final String SEP = File.separator;
	// executor to node(remote host)
	@Getter
	private Executor executor;

	// executor for Fuse on remote host
	@Getter
	private Executor fuseExecutor;
	// Product zip path
	private String productZipPath;

	// Full path to unzipped product
	private String productPath;

	/**
	 * Constructor.
	 *
	 * @param nodeClient sshClient to remote host
	 * @param fuseClient sshClient to fuse on remote host
	 * @throws SSHClientException if there is some serious problem with ssh
	 */
	public RemoteNodeManager(SSHClient nodeClient, SSHClient fuseClient) throws SSHClientException {
		this.executor = new Executor(nodeClient);
		this.fuseExecutor = new Executor(fuseClient);
		executor.connect();
	}

	@Override
	public void prepareZip() {
		log.info("Preparing zip...");
		executor.executeCommand("mkdir " + getFolder());
		productZipPath = Downloader.getProduct(executor);
		log.debug("Zip path is " + productZipPath);
	}

	@Override
	public void unzipArtifact() {
		log.info("Unzipping fuse from " + productZipPath);

		// Jar can't unzip to specified directory, so we need to change the dir first
		log.debug(executor.executeCommand("cd " + getFolder() + "; jar xf $(basename " + productZipPath + ")"));
		// Problem if WORKING_DIRECTORY is set because then the first command doesn't work

		productPath = "".equals(SystemProperty.getWorkingDirectory())
				? executor.executeCommand("ls -d $PWD" + SEP + getFolder() + SEP + "*" + SEP).trim()
				: executor.executeCommand("ls -d " + getFolder() + SEP + "*" + SEP).trim();

		log.debug("Product path is " + productPath);
		SystemProperty.set(FaframConstant.FUSE_PATH, productPath);
	}

	@Override
	public void prepareFuse() {
		ModifierExecutor.executeModifiers(executor);
	}

	@Override
	public void startFuse() {
		try {
			// TODO(rjakubco): add changing java before start
			log.info("Starting fuse");
			executor.executeCommand(productPath + SEP + "bin" + SEP + "start");
			fuseExecutor.waitForBoot();
			// TODO(avano): special usecase for remote standalone starting? maybe not necessary
			if (!SystemProperty.isFabric() && !SystemProperty.skipBrokerWait()) {
				fuseExecutor.waitForBroker();
			}
		} catch (Exception e) {
			throw new FaframException("Could not start container: " + e);
		}
	}

	@Override
	public void stopAndClean(boolean ignoreExceptions) {
		// For remote deployment just clean modifiers and System properties
		SystemProperty.clearAllProperties();
		ModifierExecutor.executePostModifiers();
		ModifierExecutor.clearAllModifiers();
	}

	/**
	 * Stops all karaf instances and removes them.
	 */
	public void clean() {
		// todo(rjakubco): create better cleaning mechanism for Fabric on Windows machines
		log.info("Killing Fuse ");
		executor.executeCommand("pkill -9 -f karaf");

		log.info("Deleting Fafram folder on  " + SystemProperty.getHost());
		executor.executeCommand("rm -rf " + SystemProperty.getFaframFolder());
	}

	/**
	 * Creates folder path on remote machines.
	 * Checking if property fafram.working.directory is set.
	 *
	 * @return path where fafram8 folder should be created
	 */
	public static String getFolder() {
		String folder;
		if ("".equals(SystemProperty.getWorkingDirectory())) {
			folder = SystemProperty.getFaframFolder();
		} else {
			folder = SystemProperty.getWorkingDirectory() + SEP + SystemProperty.getFaframFolder();
		}
		return folder;
	}

	@Override
	public void restart() {
		executor.executeCommand(productPath + SEP + "bin" + SEP + "stop");
		fuseExecutor.waitForShutdown();
		startFuse();
	}
}
