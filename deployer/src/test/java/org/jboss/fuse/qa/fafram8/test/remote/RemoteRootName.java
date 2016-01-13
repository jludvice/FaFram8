package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Remote root name test class.
 * Created by avano on 11.1.16.
 */
public class RemoteRootName {
	@Rule
	public Fafram fafram = new Fafram().name("testroot").suppressStart();

	@Test
	public void remoteRootNameTest() {
		String fileLocation = System.getProperty(FaframConstant.FUSE_PATH) + File.separator + "etc" + File.separator +
				"system.properties";

		String file = fafram.executeNodeCommand("cat " + fileLocation);
		assertTrue(file.contains("karaf.name = testroot"));
	}
}
