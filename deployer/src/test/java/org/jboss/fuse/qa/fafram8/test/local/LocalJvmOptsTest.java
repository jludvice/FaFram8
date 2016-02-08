package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Test;

import java.io.File;

/**
 * Created by avano on 8.10.15.
 */
public class LocalJvmOptsTest {
	private Fafram fafram;

	@Test
	public void jvmOptsTest() throws Exception {
		fafram = new Fafram().setup();
		assertTrue(FileUtils.readFileToString(new File(System.getProperty(FaframConstant.FUSE_PATH) + File.separator + "bin" +
				File.separator + "setenv")).contains("1536M"));
	}

	@Test
	public void customJvmOptsTest() throws Exception {
		fafram = new Fafram().setJvmOptions("312M", "512M", "312M", "512M").setup();
		assertTrue(FileUtils.readFileToString(new File(System.getProperty(FaframConstant.FUSE_PATH) + File.separator + "bin" +
				File.separator + "setenv")).contains("312M"));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
