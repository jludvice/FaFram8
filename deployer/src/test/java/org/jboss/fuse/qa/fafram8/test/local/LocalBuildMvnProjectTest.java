package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test for building maven project with custom goals.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class LocalBuildMvnProjectTest {
	@Rule
	public Fafram fafram = new Fafram().buildBundle("src/test/resources/blank-project/pom.xml", "clean", "install").suppressStart();

	@BeforeClass
	public static void before() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("user.home") + "/.m2/repositories/org/jboss/fuse/qa/test"));
	}

	@Test
	public void testProject() throws Exception {
		final File artifact = new File(System.getProperty("user.home") + "/.m2/repository/org/jboss/fuse/qa/test/useless-artifact/1.0/useless-artifact-1.0.jar");
		assertTrue(artifact.exists());
	}
}
