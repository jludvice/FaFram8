package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.io.IOUtils;

import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Random modifier class.
 * Modifier for better performance of Fuse on Openstack machines.
 * Created by avano on 16.9.15.
 */
@Slf4j
@ToString
public final class RandomModifier implements Modifier {
	@Setter
	private Executor executor;
	@Getter
	private String host;
	/**
	 * Private constructor.
	 */
	private RandomModifier() {
	}

	/**
	 * Factory method.
	 *
	 * @return random modifier instance
	 */
	public static RandomModifier changeRandomSource() {
		return new RandomModifier();
	}

	@Override
	public void execute() {
		if (executor == null) {
			localExecute();
		} else {
			remoteExecute();
		}
	}

	/**
	 * Adds random modifier to bin/karaf on localhost.
	 */
	public void localExecute() {
		try {
			final String filePath = SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv";
			final FileInputStream fis = new FileInputStream(filePath);
			String content = IOUtils.toString(fis);

			// Default java opts from karaf + randomness location
			content += "\nexport JAVA_OPTS=\"-Xms$JAVA_MIN_MEM -Xmx$JAVA_MAX_MEM -XX:+UnlockDiagnosticVMOptions -XX:+UnsyncloadClass -Djava"
					+ ".security.egd=file:/dev/./urandom\"";
			final FileOutputStream fos = new FileOutputStream(filePath, false);
			IOUtils.write(content, fos);

			fis.close();
			fos.close();
		} catch (Exception ex) {
			log.error("Error while manipulating the files " + ex);
		}
	}

	/**
	 * Adds random modifier to bin/karaf on remote host.
	 */
	public void remoteExecute() {
		final String filePath = SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv";

		final String response = executor.executeCommand("printf \" \nexport JAVA_OPTS=\\\"-Xms\\$JAVA_MIN_MEM -Xmx\\$JAVA_MAX_MEM "
				+ "-XX:+UnlockDiagnosticVMOptions -XX:+UnsyncloadClass -Djava.security.egd=file:/dev/./urandom\\\" \" >> " + filePath);
		if (!response.isEmpty()) {
			log.error("Setting property on remote host failed. Response should be empty but was: {}.", response);
		}
	}
}
