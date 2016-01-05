package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Remote SSH Container test.
 * Created by ecervena on 9/8/15.
 */
public class RemoteSSHContainerTest {
	@Rule
	public Fafram fafram = new Fafram().withFabric();

	@AfterClass
	public static void clean() {
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}

	@Test
	@Ignore
	public void fabricTest() {
		assertTrue(fafram.executeCommand("container-list | grep root").contains("success"));
		assertTrue(fafram.executeCommand("container-list | grep node3").contains("success"));
	}

	static {
		// TODO(ecervena): machine is dead
		System.setProperty(FaframConstant.HOST, "10.8.49.151");
		System.setProperty(FaframConstant.FUSE_ZIP, "file:///home/fuse/storage/jboss-fuse-full-6.2.1.redhat-084.zip");
	}
}
