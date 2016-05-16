package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.apache.tools.ant.DirectoryScanner;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

/**
 * Test for testing archiving specific files from remote.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class RemoteArchive {
	private static String firstTestPath = "";

	@Test
	public void testA() {
		final Fafram fafram = new Fafram().suppressStart().archive("etc/users.properties").setup();
		final String baseDir = System.getProperty("user.dir");
		fafram.tearDown();

		final String path = Paths.get(baseDir, "target", "archived").toAbsolutePath().toString();
		// setup Ant Directory Scanner
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] {"*/users.properties"});
		// set base dir to target/
		scanner.setBasedir(path);
		scanner.setCaseSensitive(false);
		// perform scan
		scanner.scan();
		final String[] foundFiles = scanner.getIncludedFiles();

		// There should be only 1 found file
		assertTrue("There should be 1 found file but was " + foundFiles.length, foundFiles.length == 1);
		assertTrue("File :" + path + File.separator + foundFiles[0] + " doesn't exists!", new File(path + File.separator + foundFiles[0]).exists());
		firstTestPath = path + File.separator + foundFiles[0];
	}

	@Test
	public void testB() {
		final Fafram fafram = new Fafram().suppressStart().archive("etc/users.properties").setup();
		final String baseDir = System.getProperty("user.dir");

		fafram.tearDown();
		final String path = Paths.get(baseDir, "target", "archived").toAbsolutePath().toString();

		// setup Ant Directory Scanner
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] {"*/users.properties"});
		// set base dir to target/
		scanner.setBasedir(path);
		scanner.setCaseSensitive(false);
		// perform scan
		scanner.scan();
		final String[] foundFiles = scanner.getIncludedFiles();

		// There should be only 1 found file
		assertTrue("There should be 2 found file but was " + foundFiles.length, foundFiles.length == 2);
		// Assert that the first test archive is still present
		assertTrue("Previous archived file isn't there anymore", new File(firstTestPath).exists());
	}

	@Test
	public void testC() {
		final Fafram fafram = new Fafram().suppressStart().archive("etc/*.xml").setup();
		final String baseDir = System.getProperty("user.dir");

		fafram.tearDown();
		final String path = Paths.get(baseDir, "target", "archived").toAbsolutePath().toString();

		// setup Ant Directory Scanner
		final DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(new String[] {"*/*.xml"});
		// set base dir to target/
		scanner.setBasedir(path);
		scanner.setCaseSensitive(false);
		// perform scan
		scanner.scan();
		final String[] foundFiles = scanner.getIncludedFiles();

		// There should be only 1 found file
		assertTrue("There should be 2 found file but was " + foundFiles.length, foundFiles.length == 2);
	}
}
