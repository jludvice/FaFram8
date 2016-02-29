package org.jboss.fuse.qa.fafram8.modifier.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

import org.jboss.fuse.qa.fafram8.modifier.Modifier;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Archive modifier class.
 * Created by avano on 8.10.15.
 */
@Slf4j
@ToString
@EqualsAndHashCode(callSuper = true)
public class ArchiveModifier extends Modifier {
	private Path archiveTargetPath = Paths.get(SystemProperty.getArchiveTarget()).toAbsolutePath();
	private String[] archiveFiles = SystemProperty.getArchivePattern().split(" *, " + "*"); //ignore spaces around comma

	@Override
	public void execute() {
		if (archiveFiles.length == 0) {
			log.info("Nothing to archive.");
			return;
		}

		if (super.getExecutor() != null) {
			archiveRemoteFiles();
		} else {
			archiveLocalFiles();
		}
	}

	/**
	 * Archives files on localhost.
	 */
	private void archiveLocalFiles() {
		log.info("Archiving files with patterns: {}", archiveFiles);

		try {
			// setup Ant Directory Scanner
			final DirectoryScanner scanner = new DirectoryScanner();
			scanner.setIncludes(archiveFiles);
			// set base dir to target/
			scanner.setBasedir(SystemProperty.getFusePath());
			scanner.setCaseSensitive(false);
			// perform scan
			scanner.scan();
			final String[] foundFiles = scanner.getIncludedFiles();

			log.info("Archiving {} file" + (foundFiles.length > 1 ? "s" : "") + " to {}", foundFiles.length, archiveTargetPath);
			for (String fileName : foundFiles) {
				//scanner returns paths relative to fuseDir
				final Path p = Paths.get(SystemProperty.getFusePath(), fileName);
				log.debug("Archiving file {}", fileName);
				//create target directory structure
				final Path target = Paths.get(archiveTargetPath.toString(), StringUtils.substringBetween(
						Paths.get(SystemProperty.getFusePath(), fileName).toAbsolutePath().toString(), SystemProperty.getBaseDir(), fileName),
						fileName).toAbsolutePath();
				Files.createDirectories(target.getParent());
				// for instance copy
				// from: $FUSE_HOME/data/log/fuse.log
				// to: target/archived/data/log/fuse.log
				Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			log.error("Failed to archive files with following patterns: {}", archiveFiles, e);
			// Don't throw the exception here, the properties and modifiers will not unset
		}
	}

	/**
	 * Archives files on remote.
	 */
	private void archiveRemoteFiles() {
		// TODO(avano): probably get from all machines using jsch scp
	}

	/**
	 * Factory method.
	 *
	 * @return new archive modifier instance
	 */
	public static ArchiveModifier registerArchiver() {
		return new ArchiveModifier();
	}
}
