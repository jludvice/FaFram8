package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * JVM Opts modifier.
 * Created by avano on 8.10.15.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Slf4j
@EqualsAndHashCode(callSuper = true)
public final class JvmMemoryModifier extends Modifier {
	private String xms = "768M";
	private String xmx = "1536M";
	private String permMem = "768M";
	private String maxPermMem = "1536M";

	private List<String> jvmOpts = new ArrayList<>();

	/**
	 * Private constructor.
	 */
	private JvmMemoryModifier() {
		jvmOpts.add("JAVA_MIN_MEM=" + xms);
		jvmOpts.add("JAVA_MAX_MEM=" + xmx);
		jvmOpts.add("JAVA_PERM_MEM=" + permMem);
		jvmOpts.add("JAVA_MAX_PERM_MEM=" + maxPermMem);
	}

	private JvmMemoryModifier(List<String> jvmOpts) {
		this.jvmOpts.addAll(jvmOpts);
	}

	@Override
	public void execute() {
		if (super.getExecutor() != null) {
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
	public static JvmMemoryModifier setDefaultJvmOpts() {
		return new JvmMemoryModifier();
	}

	public static JvmMemoryModifier setJvmOpts(List<String> jvmOpts) {
		return new JvmMemoryModifier(jvmOpts);
	}

	/**
	 * Modifies JVM Opts on localhost.
	 */
	private void modifyLocalJvmOpts() {
		// Files locations
		final File setenv = new File(SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv");
		final File setenvBat = new File(SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv.bat");

		try {
			if (!setenvBat.exists()) {
				setenvBat.createNewFile();
			}
			if (!setenv.exists()) {
				setenv.createNewFile();
			}
			if ((System.getProperty("os.name").startsWith("Windows"))) {
				for (String line : jvmOpts) {
					FileUtils.writeStringToFile(setenvBat, "export " + line, true);
				}
			} else {
				for (String line : jvmOpts) {
					FileUtils.writeStringToFile(setenv, "export " + line, true);
				}
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
		final StringBuilder builder = new StringBuilder();
		for (String line : jvmOpts) {
			builder.append(String.format("export %s%n", line));
		}
		String content = builder.toString();
		// Remove original files
		if ((System.getProperty("os.name").startsWith("Windows"))) {
			// Changes to win
			content = content.replaceAll("export", "SET");
			super.getExecutor().executeCommand("printf \"" + content + "\" >> " + path + ".bat");
		} else {
			// Print content into the files
			super.getExecutor().executeCommand("printf \"" + content + "\" >> " + path);
		}
	}

	public String toString() {
		return "org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier(jvmOpts=" + this.jvmOpts + ")";
	}
}
