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

	private List<String> jvmMemOpts = new ArrayList<>();

	/**
	 * Private constructor.
	 */
	private JvmMemoryModifier() {
		jvmMemOpts.add("JAVA_MIN_MEM=" + xms);
		jvmMemOpts.add("JAVA_MAX_MEM=" + xmx);
		jvmMemOpts.add("JAVA_PERM_MEM=" + permMem);
		jvmMemOpts.add("JAVA_MAX_PERM_MEM=" + maxPermMem);
	}

	/**
	 * Private constructor.
	 *
	 * @param jvmOpts JVM options
	 */
	private JvmMemoryModifier(List<String> jvmOpts) {
		this.jvmMemOpts.addAll(jvmOpts);
	}

	@Override
	public void execute() {
		if (super.getExecutor() != null) {
			modifyRemoteJvmMemOpts();
		} else {
			modifyLocalJvmMemOpts();
		}
	}

	/**
	 * Sets default jvm options.
	 *
	 * @return jvm options modifier
	 */
	public static JvmMemoryModifier setDefaultJvmMemOpts() {
		return new JvmMemoryModifier();
	}

	/**
	 * Sets specified JVM memory opts.
	 *
	 * @param jvmMemOpts jvm memory opts for setting
	 * @return jvm options modifier
	 */
	public static JvmMemoryModifier setJvmMemOpts(List<String> jvmMemOpts) {
		return new JvmMemoryModifier(jvmMemOpts);
	}

	/**
	 * Modifies JVM Opts on localhost.
	 */
	private void modifyLocalJvmMemOpts() {
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
				for (String line : jvmMemOpts) {
					FileUtils.writeStringToFile(setenvBat, "export " + line, true);
				}
			} else {
				for (String line : jvmMemOpts) {
					FileUtils.writeStringToFile(setenv, "export " + line, true);
				}
			}
		} catch (Exception ex) {
			log.error("Exception while modifying files: " + ex);
			throw new FaframException(ex);
		}
	}

	/**
	 * Modifies JVM memory opts on remote.
	 */
	private void modifyRemoteJvmMemOpts() {
		final String path = SystemProperty.getFusePath() + File.separator + "bin" + File.separator + "setenv";
		final StringBuilder builder = new StringBuilder();
		for (String line : jvmMemOpts) {
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
		return "org.jboss.fuse.qa.fafram8.modifier.impl.JvmOptsModifier(jvmMemoryOpts=" + this.jvmMemOpts + ")";
	}
}
