package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Test;

import java.io.File;

/**
 * Local keep folder test.
 * Created by avano on 21.8.15.
 */
public class LocalKeepFolderTest {
	private Fafram fafram;

	@Test
	public void keepFolderTest() {
		fafram = new Fafram().suppressStart().keepFolder().setup();

		// It sets the system property to the product path
		final String path = fafram.getProductPath();
		fafram.tearDown();

		assertTrue("Folder was deleted", new File(path).exists());
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
