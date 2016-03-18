package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Remote replace file test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteReplaceFile {

	@Rule
	public Fafram fafram = new Fafram().replaceFile("etc/activemq.xml", "target/test-classes/replace.xml").suppressStart();

	@Test
	public void replaceFileTest() {
		String replaceContent = null;
		try {
			replaceContent = FileUtils.readFileToString(new File("target/test-classes/replace.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertNotNull(replaceContent);

		final String fileLocation = fafram.getProductPath() + File.separator + "etc" + File.separator
				+ "activemq.xml";

		final String file = fafram.executeNodeCommand("cat " + fileLocation);

		assertNotNull(file);
		assertEquals(replaceContent, file);
	}
}
