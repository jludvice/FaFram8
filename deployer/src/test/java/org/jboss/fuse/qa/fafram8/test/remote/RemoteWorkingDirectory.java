package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * Test for testing WORKING_DIRECTORY property on remote machine.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
public class RemoteWorkingDirectory {
	private final static String DIR = "test/working/directory";
	Fafram fafram;

	@Test
	public void testBasicRemote() throws Exception {
		SSHClient nodeSSHClient = new NodeSSHClient().host(SystemProperty.getHost()).port(SystemProperty.getHostPort())
				.username(SystemProperty.getHostUser()).password(SystemProperty.getHostPassword());
		nodeSSHClient.connect(true);
		nodeSSHClient.executeCommand("mkdir -p " + DIR, true);

		String folder = nodeSSHClient.executeCommand("pwd", true);
		System.setProperty(FaframConstant.WORKING_DIRECTORY, folder + File.separator + DIR);

		fafram = new Fafram().setup();

		assertTrue(SystemProperty.getFusePath().contains(DIR));
	}

	@After
	public void tearDown() throws Exception {
		System.clearProperty(FaframConstant.WORKING_DIRECTORY);
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
