package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Root name test.
 * Created by avano on 11.1.16.
 */
public class LocalRootNameTest {
	@Rule
	public Fafram fafram = new Fafram().name("testroot").suppressStart();

	@Test
	public void rootNameTest() throws Exception {
		final String fileContent = FileUtils.readFileToString(new File(fafram.getProductPath() + File.separator + "etc" +
				File.separator + "system.properties"));
		assertTrue(fileContent.contains("karaf.name = testroot"));
		assertFalse(fileContent.contains("karaf.name = root"));
	}
}
