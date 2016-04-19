package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
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
	private static String childName = "build-child-container";
	private static String sshName = "build-ssh-container";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	private Container root = RootContainer.builder().defaultRoot().name("build-root").withFabric().build();
	private Container child = ChildContainer.builder().name(childName).parent(root).build();
	private Container ssh = SshContainer.builder().name(sshName).parent(root).build();

	@Rule
	public Fafram fafram = new Fafram().fuseZip(FaframTestBase.CURRENT_HTTP_URL).provider(FaframProvider.OPENSTACK).containers(root, child, ssh);

	@Test
	public void killTest() throws Exception {
		child.kill();
		String response = fafram.executeCommand("exec ps aux | grep " + childName);
		assertFalse(response.contains("karaf.base"));

		final Executor executor = ssh.getNode().getExecutor();

		executor.executeCommand("pkill -9 -f karaf");
		response = executor.executeCommand("ps aux | grep " + sshName);
		assertFalse(response.contains("karaf.base"));

		root.kill();

		assertNull(fafram.executeCommand("list"));
	}

	@AfterClass
	public static void tearDown() {
		osm.releaseResources();
	}
}
