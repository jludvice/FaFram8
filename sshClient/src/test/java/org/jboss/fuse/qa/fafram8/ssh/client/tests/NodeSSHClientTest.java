package org.jboss.fuse.qa.fafram8.ssh.client.tests;

import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class NodeSSHClientTest {

	private static final String HOST = "10.8.50.177";

	@Ignore
	@Test
	public void testFluent() throws Exception {
		Assert.fail("TODO rjakubco");
		final SSHClient client = new NodeSSHClient().host(HOST).defaultSSHPort().username("admin")
				.password("admin");
		client.connect(false);
		final String response = client.executeCommand("ping -c 1 google.com", false);
		Assert.assertTrue(response.contains("PING google.com"));
	}

	@Ignore
	@Test
	public void testSetters() throws Exception {
		Assert.fail("TODO(rjakubco): implement");
		// everything wrong -> check if setters work correctly
		final SSHClient client = new NodeSSHClient().host("10.8.50.17").fuseSSHPort().username("admin")
				.password("admin");

		client.setHost(HOST);
		client.setPort(22);
		client.setUsername("fuse");
		client.setPassword("fuse");

		client.connect(false);

		final String response = client.executeCommand("ping -c 1 google.com", false);
		Assert.assertTrue(response.contains("PING google.com"));
	}

	@Ignore
	@Test//(expected = SSHClientException.class)
	public void testWrongPort() throws Exception {
		Assert.fail("TODO(rjakubco): implement");
		final SSHClient client = new NodeSSHClient().host("10.8.50.17").port(50).username("admin")
				.password("fuse");
		client.connect(false);
		System.out.println(client.executeCommand("ping -c 1 google.com", false));
	}

	@Ignore
	@Test//(expected = SSHClientException.class)
	public void testWrongHost() throws Exception {
		Assert.fail("TODO(rjakubco): implement");
		final SSHClient client = new NodeSSHClient().host("10.8.50.17").defaultSSHPort().username("admin")
				.password("fuse");
		client.connect(false);
		System.out.println(client.executeCommand("ping -c 1 google.com", false));
	}

	@Ignore
	@Test//(expected = SSHClientException.class)
	public void testAuthFail() throws Exception {
		Assert.fail("TODO(rjakubco): implement");
		final SSHClient client = new NodeSSHClient().host(HOST).defaultSSHPort().username("admin")
				.password("fuse");
		client.connect(false);
		System.out.println(client.executeCommand("ping -c 1 google.com", false));
	}

	public void testCopyFile() {

	}

	public void testReadRemoteFile() {

	}
}
