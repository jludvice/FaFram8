package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * JVM Opts modifier.
 * Created by avano on 8.10.15.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
@ToString
public final class JvmOptsModifier implements Modifier {
	private String xms = "768M";
	private String xmx = "1536M";
	private String permMem = "768M";
	private String maxPermMem = "1536M";
	@Setter
	private Executor executor;

	/**
	 * Private constructor.
	 */
	private JvmOptsModifier() {
	}

	/**
	 * Private constructor.
	 *
	 * @param xms xms
	 * @param xmx xmx
	 * @param permMem perm mem
	 * @param maxPermMem max perm mem
	 */
	private JvmOptsModifier(String xms, String xmx, String permMem, String maxPermMem) {
		this.xms = xms;
		this.xmx = xmx;
		this.permMem = permMem;
		this.maxPermMem = maxPermMem;
	}

	@Override
	public void execute() {
		if (executor != null) {
			modifyRemoteJvmOpts();
		} else {
			modifyLocalJvmOpts();
		}
	}

	/**
	 * Sets default jvm options.
	 *
	 * @return jvm options modifier
	 */
	public static JvmOptsModifier setDefaultJvmOpts() {
		return new JvmOptsModifier();
	}

	/**
	 * Sets jvm options.
	 *
	 * @param xms xms
	 * @param xmx xmx
	 * @param permMem perm mem
	 * @param maxPermMem max perm mem
	 * @return jvm options modifier
	 */
	public static JvmOptsModifier setJvmOpts(String xms, String xmx, String permMem, String maxPermMem) {
		return new JvmOptsModifier(xms, xmx, permMem, maxPermMem);
	}

	/**
	 * Modifies JVM Opts on localhost.
	 */
	private void modifyLocalJvmOpts() {
		// Files locations
		final File setenv = new File(SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv");
		final File setenvBat = new File(SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv.bat");

		// File content
		final List<String> lines = Arrays.asList("JAVA_MIN_MEM=" + xms + "\n", "JAVA_MAX_MEM=" + xmx + "\n",
				"JAVA_PERM_MEM=" + permMem + "\n", "JAVA_MAX_PERM_MEM=" + maxPermMem);
		try {
			if (!setenv.exists()) {
				setenv.createNewFile();
			}
			if (!setenvBat.exists()) {
				setenvBat.createNewFile();
			}
			for (String line : lines) {
				FileUtils.writeStringToFile(setenv, "export " + line, true);
				FileUtils.writeStringToFile(setenvBat, "SET " + line, true);
			}
		} catch (Exception ex) {
			log.error("Exception while modifying files: " + ex);
			throw new FaframException(ex);
		}
	}

	/**
	 * Modifies JVM opts on remote.
	 */
	private void modifyRemoteJvmOpts() {
		final String path = SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv";

		// Remove original files
		executor.executeCommand("rm -rf " + path);
		executor.executeCommand("rm -rf " + path + ".bat");

		// Print content into the files
		String content = String.format("export JAVA_MIN_MEM=%s%nexport JAVA_MAX_MEM=%s%nexport JAVA_PERM_MEM=%s%nexport JAVA_MAX_PERM_MEM=%s",
				xms, xmx, permMem, maxPermMem);
		executor.executeCommand("printf \"" + content + "\" >> " + path);

		// Changes to win
		content = content.replaceAll("export", "SET");
		executor.executeCommand("printf \"" + content + "\" >> " + path + ".bat");
	}
}
