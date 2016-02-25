package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.io.File;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Access rights modifier.
 * Created by avano on 20.8.15.
 */
@ToString
@EqualsAndHashCode(callSuper = true, exclude = {"executor"})
public final class AccessRightsModifier extends Modifier {
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
		if (super.getExecutor() == null) {
			localExecute();
		} else {
			remoteExecute();
		}
	}

	/**
	 * Executes the modifier on localhost.
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
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
			super.getExecutor().executeCommand("chmod +x " + SystemProperty.getFusePath() + path);
		}
	}
}
