package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Local custom zip test.
 * Created by avano on 21.8.15.
 */
public class LocalCustomZipTest {
	private Fafram fafram;

	@Before
	public void init() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:///home/fuse/storage/jboss-fuse-full-6.1.0.redhat-379.zip");
	}

	@Test
	public void customZipTest() {
		fafram = new Fafram();
		fafram.setup();

		String artifactName = SystemProperty.getFuseZip().substring(SystemProperty.getFuseZip().lastIndexOf("/") + 1);

		// Fuse zip contains 'full' in the zip, but not in folder name
		artifactName = artifactName.replace("full-", "");

		// Remove extension
		String expectedFolderName = artifactName.substring(0, artifactName.lastIndexOf("."));

		// It sets the system property to the product path
		assertTrue("Does not contain expected dir in path",
				System.getProperty(FaframConstant.FUSE_PATH).contains(expectedFolderName));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}

		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}