package org.jboss.fuse.qa.fafram8.ssh.client.tests;

import org.jboss.fuse.qa.fafram8.ssh.FuseSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class FuseSSHClientTest {
	private static final String HOST = "10.8.50.177";

	// TODO(rjakubco): need to test this somehow -> find easiest solution
	@Ignore
	@Test
	public void testFuseSSHFluent() throws Exception {
		Assert.fail("TODO rjakubco");
		final SSHClient client = new FuseSSHClient().host(HOST).fuseSSHPort().username("admin")
				.password("admin");
		client.connect(false);
		final String response = client.executeCommand("ff", false);
		System.out.println(response);
//		Assert.assertTrue(response.contains("PING google.com"));
	}

	@Ignore
	@Test
	public void testFuseSSHSetters() throws Exception {
		Assert.fail("TODO rjakubco");
	}

	@Ignore
	@Test
	public void testWrongPort() throws Exception {
		Assert.fail("TODO rjakubco");
	}

	@Ignore
	@Test
	public void testWrongHost() throws Exception {
		Assert.fail("TODO rjakubco");
	}

	@Ignore
	@Test
	public void testAuthFail() throws Exception {
		Assert.fail("TODO rjakubco");
	}
}
