package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertFalse;

import org.apache.commons.io.FileUtils;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Skip default user test.
 * Created by avano on 5.10.15.
 */
public class LocalWithoutDefaultUserTest {

	@Rule
	public Fafram fafram = new Fafram().withoutDefaultUser().addUser("testUser", "testPassword", "admin,manager,viewer,Monitor, Operator, " +
			"Maintainer, Deployer, Auditor, Administrator, SuperUser").suppressStart();

	@AfterClass
	public static void afterClass() {
		System.clearProperty(FaframConstant.FUSE_USER);
		System.clearProperty(FaframConstant.FUSE_PASSWORD);
	}

	@Test
	public void customUserTest() throws Exception {
		String fileContent = FileUtils.readFileToString(new File(fafram.getProductPath() + File.separator + "etc" +
				File.separator + "users.properties"));

		// Change the default commented out admin user
		fileContent = fileContent.replaceAll("#admin", "ignored");
		assertFalse(fileContent.contains("admin=admin"));
	}

	static {
		System.setProperty(FaframConstant.FUSE_USER, "testUser");
		System.setProperty(FaframConstant.FUSE_PASSWORD, "testPassword");
	}
}
