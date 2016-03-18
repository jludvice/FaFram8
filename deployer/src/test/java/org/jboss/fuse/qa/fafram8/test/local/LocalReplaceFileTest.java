package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Local replace file test.
 * Created by avano on 21.8.15.
 */
public class LocalReplaceFileTest {
	@Rule
	public Fafram fafram = new Fafram().replaceFile("fabric/readme.txt", "target/test-classes/replace.xml").suppressStart();

	@Test
	public void replaceFileTest() {
		String replaceContent = null;
		try {
			replaceContent = FileUtils.readFileToString(new File("target/test-classes/replace.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertNotNull(replaceContent);

		final String fileLocation = fafram.getProductPath() + File.separator + "fabric" + File.separator + "readme.txt";
		String file = null;
		try {
			file = FileUtils.readFileToString(new File(fileLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertNotNull(file);
		assertEquals(replaceContent, file);
	}
}
