package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import java.io.File;
import java.io.IOException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Root name modifier class. This class gets the FaframConstant.FAFRAM_ROOT_NAMES property, parses it and sets the root names
 * on both local and remote nodes.
 * Created by avano on 11.1.16.
 */
@Slf4j
public final class RootNamesModifier implements Modifier {
	private static final int IP_POS = 0;
	private static final int USER_POS = 1;
	private static final int PASS_POS = 2;
	private static final int NAME_POS = 3;
	@Setter
	private Executor executor;

	/**
	 * Private constructor.
	 */
	private RootNamesModifier() {
	}

	/**
	 * Factory method.
	 *
	 * @return random modifier instance
	 */
	public static RootNamesModifier setRootNames() {
		return new RootNamesModifier();
	}

	@Override
	public void execute() {
		final String[] content = SystemProperty.getRootNames();

		if (content == null) {
			return;
		}

		for (String s : content) {
			final String[] root = s.split(",");
			if ("localhost".equals(root[IP_POS])) {
				final File configFile = new File(SystemProperty.getFusePath() + File.separator + "etc" + File.separator + "system.properties");
				try {
					String fileContent = FileUtils.readFileToString(configFile);
					fileContent = fileContent.replaceAll("karaf.name = root", "karaf.name = " + root[NAME_POS]);
					FileUtils.write(configFile, fileContent);
				} catch (IOException e) {
					throw new FaframException("Error while setting root name: " + e);
				}
			} else {
				modifyRemoteRootName(root);
			}
		}
	}

	/**
	 * Modifies the root name on remote. Creates a new ssh client, connects to the IP using supplied credentials
	 * and modifies the remote system properties file.
	 *
	 * @param csv csv record for the root name
	 */
	private void modifyRemoteRootName(String[] csv) {
		final String ip = csv[IP_POS];
		final String user = csv[USER_POS];
		final String pass = csv[PASS_POS];
		final String name = csv[NAME_POS];
		final int defaultPort = 22;
		final SSHClient c = new NodeSSHClient();
		c.setHostname(ip);
		c.setPort(defaultPort);
		c.setUsername(user);
		c.setPassword(pass);
		try {
			c.connect(true);
			log.debug("Setting root name to " + name + " on " + ip);
			c.executeCommand("sed -i 's#\\<karaf.name = root\\>#karaf.name = " + name + "#g' "
					+ SystemProperty.getFusePath() + "etc" + File.separator + "system.properties", true);
			c.disconnect();
		} catch (Exception e) {
			throw new FaframException("Error while setting root name on " + ip + ": " + e);
		}
	}

	@Override
	public String toString() {
		final String[] content = SystemProperty.getRootNames();
		String toString = "";
		if (content != null) {
			for (String s : content) {
				final String[] root = s.split(",");
				toString += "".equals(toString) ? "" : ",";
				toString += root[NAME_POS] + "@" + root[IP_POS];
			}
		}
		return "RootNamesModifier(" + toString + ')';
	}
}
