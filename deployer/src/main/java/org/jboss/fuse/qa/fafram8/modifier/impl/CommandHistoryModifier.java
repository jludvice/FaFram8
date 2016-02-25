package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.util.CommandHistory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Command history post modifier class.
 * Created by avano on 24.11.15.
 */
@Slf4j
@ToString
@EqualsAndHashCode(callSuper = true)
public final class CommandHistoryModifier extends Modifier {
	/**
	 * Private constructor.
	 */
	private CommandHistoryModifier() {
	}

	/**
	 * Factory method - command for dumping commands history into the file.
	 *
	 * @return command instance
	 */
	public static CommandHistoryModifier saveCommandHistory() {
		return new CommandHistoryModifier();
	}

	@Override
	public void execute() {
		// Do nothing if there are no commands
		if ("".equals(CommandHistory.dumpCommands())) {
			return;
		}

		// Archive the command history to the archive target directory
		final File f = new File(Paths.get(SystemProperty.getArchiveTarget(), new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
				.format(new Date()) + ".txt").toAbsolutePath().toString());

		try {
			FileUtils.write(f, CommandHistory.dumpCommands());

			// It's dumped, we don't need it anymore (I hope)
			CommandHistory.clean();
		} catch (IOException ex) {
			log.error("Error while dumping commands: " + ex);
		}
	}
}
