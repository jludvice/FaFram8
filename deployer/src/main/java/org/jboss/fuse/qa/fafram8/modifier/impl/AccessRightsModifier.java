package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Access rights modifier.
 * Created by avano on 20.8.15.
 */
@ToString
public final class AccessRightsModifier implements Modifier {
	@Setter
	private Executor executor;

	@Getter
	private String host;
	@SuppressWarnings("MismatchedReadAndWriteOfArray")
	private String[] paths;

	/**
	 * Private constructor.
	 *
	 * @param paths paths
	 */
	private AccessRightsModifier(String[] paths) {
		this.paths = paths;
	}

	/**
	 * Factory method - command for changing access rights.
	 *
	 * @param paths array of paths
	 * @return command instance
	 */
	public static AccessRightsModifier setExecutable(String... paths) {
		return new AccessRightsModifier(paths);
	}

	@Override
	public void execute() {
		if (executor == null) {
			localExecute();
		} else {
			remoteExecute();
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	/**
	 * Executes the modifier on localhost.
	 */
	private void localExecute() {
		for (String path : paths) {
			new File(SystemProperty.getFusePath() + File.separator + path).setExecutable(true);
		}
	}

	/**
	 * Executes the modifier on remote.
	 */
	private void remoteExecute() {
		for (String path : paths) {
			executor.executeCommand("chmod +x " + SystemProperty.getFusePath() + path);
		}
	}
}
