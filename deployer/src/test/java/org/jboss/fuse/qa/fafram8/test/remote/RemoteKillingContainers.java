package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests killing of child, ssh and root container by fafram.killContainer() method.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
public class RemoteKillingContainers {
	public static final String SSH_NAME = "KillingTest";
	public static String ipSsh = "";
	public static String childName = "child-container";
	public static String sshName = "ssh-container";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SSH_NAME);
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(SSH_NAME).getId());
		log.info("Testing node on Openstack spawned on IP address " + ipSsh);
		Thread.sleep(30000);
	}

	@Rule
	public Fafram fafram = new Fafram().withFabric().getBuilder().child(childName).addToFafram().ssh(sshName).nodeSsh(ipSsh, "fuse", "fuse").addToFafram().getFafram();

	@Test
	public void testName() throws Exception {
		fafram.killContainer(childName);
		String response = fafram.executeCommand("exec ps aux | grep " + childName);
		assertFalse(response.contains("karaf.base"));

		SSHClient nodeSSHClient = new NodeSSHClient().hostname(ipSsh).username("fuse").password("fuse").defaultSSHPort();
		nodeSSHClient.connect(true);

		fafram.killContainer(sshName);
		response = nodeSSHClient.executeCommand("ps aux | grep " + sshName, true);
		assertFalse(response.contains("karaf.base"));

		fafram.killContainer("root");

		assertNull(fafram.executeCommand("list"));
	}

	@AfterClass
	public static void after() {
		osm.releaseResources();
	}
}
