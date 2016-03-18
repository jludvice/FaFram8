package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Local add user test.
 * Created by avano on 21.8.15.
 */
public class LocalAddUserTest {
	private Fafram fafram;

	@Test
	public void userTest() throws IOException {
		fafram = new Fafram().addUser("testu", "testp", "testr1,testr2").suppressStart();
		fafram.setup();
		final String fileContent = FileUtils.readFileToString(new File(fafram.getProductPath() + File.separator
				+ "etc" + File.separator + "users.properties"));
		assertTrue(fileContent.contains("testu=testp,testr1,testr2"));
	}

	@Test
	public void testOverrideUser() throws Exception {
		fafram = new Fafram().addUser("fafram", "faframoverride", "Monitor").suppressStart();
		fafram.setup();
		final String fileContent = FileUtils.readFileToString(new File(fafram.getProductPath() + File.separator
				+ "etc" + File.separator + "users.properties"));
		assertTrue(fileContent.contains("fafram=faframoverride,Monitor"));
		assertFalse(fileContent.contains("fafram=fafram,Administrator"));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
