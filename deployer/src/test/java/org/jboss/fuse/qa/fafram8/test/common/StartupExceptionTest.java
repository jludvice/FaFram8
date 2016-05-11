package org.jboss.fuse.qa.fafram8.test.common;

import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test if the correct exception is thrown.
 * Created by avano on 18.9.15.
 */
public class StartupExceptionTest {
	private Fafram fafram;

	@Before
	public void before() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:///home/fuse/patches/test.zip");
	}

	@Test(expected = FaframException.class)
	public void keepFolderTest() {
		fafram = new Fafram();
		fafram.setup();
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}

		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
