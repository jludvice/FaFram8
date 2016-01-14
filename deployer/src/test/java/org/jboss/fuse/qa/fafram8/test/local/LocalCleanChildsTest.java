package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Test;

import java.io.File;

/**
 * Local clean childs test.
 * Created by avano on 14.1.16.
 */
public class LocalCleanChildsTest {
	private Fafram fafram = new Fafram().withFabric().command("container-create-child root test1", "container-create-child root test2");

	@Test
	public void cleanChildsTest() {
		fafram.setup();
		assertTrue("Container list does not contain test1", fafram.executeCommand("container-list").contains("test1"));
		assertTrue("Container list does not contain test1", fafram.executeCommand("container-list").contains("test1"));
		String path = System.getProperty(FaframConstant.FUSE_PATH);
		fafram.tearDown();

		// If the childs are deleted successfully, the whole folder is deleted successfully
		assertFalse("Folder still exists!", new File(path).exists());
	}
}
