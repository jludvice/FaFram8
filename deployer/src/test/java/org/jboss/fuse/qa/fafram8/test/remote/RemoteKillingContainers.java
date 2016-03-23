package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

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
	private static String ipSsh = "";
	private static String childName = "build-child-container";
	private static String sshName = "build-ssh-container";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	private Container root = RootContainer.builder().defaultRoot().name("build-root").withFabric().build();
	private Container child = ChildContainer.builder().name(childName).parent(root).build();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SSH_NAME);
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(SSH_NAME).getId());
		log.info("Testing node on Openstack spawned on IP address " + ipSsh);
		Thread.sleep(30000);
	}

	@Rule
	public Fafram fafram = new Fafram().fuseZip(FaframTestBase.CURRENT_URL).provider(FaframProvider.OPENSTACK).containers(root, child,
			SshContainer.builder().name(sshName).parent(root).node(Node.builder().host(ipSsh).username("fuse").password("fuse").build()).build());

	@Test
	public void killTest() throws Exception {
		child.kill();
		String response = fafram.executeCommand("exec ps aux | grep " + childName);
		assertFalse(response.contains("karaf.base"));

		final SSHClient nodeSSHClient = new NodeSSHClient().host(ipSsh).username("fuse").password("fuse").defaultSSHPort();
		nodeSSHClient.connect(true);

		nodeSSHClient.executeCommand("pkill -9 -f karaf", false);
		response = nodeSSHClient.executeCommand("ps aux | grep " + sshName, true);
		assertFalse(response.contains("karaf.base"));

		root.kill();

		assertNull(fafram.executeCommand("list"));
	}

	@AfterClass
	public static void tearDown() {
		osm.releaseResources();
	}
}
