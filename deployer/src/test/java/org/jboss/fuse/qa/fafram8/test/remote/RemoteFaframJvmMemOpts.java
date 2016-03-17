package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Test;

import java.io.File;

/**
 * Remote JVM Opts test.
 * <p/>
 * Created by avano on 8.10.15.
 */
public class RemoteFaframJvmMemOpts {

	private Fafram fafram;

	@Test
	public void jvmMemOptsTest() throws Exception {
		fafram = new Fafram().suppressStart().setup();
		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "bin" +
				File.separator + "setenv").contains("1536M"));
	}

	@Test
	public void customJvmMemOptsTest() throws Exception {
		fafram = new Fafram().setMemoryJvmOptions("312M", "512M", "312M", "512M").suppressStart().setup();
		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "bin" +
				File.separator + "setenv").contains("312M"));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
