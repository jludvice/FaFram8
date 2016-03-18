package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.property.Openstack;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Test for setting default JDK path to all types of containers.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
public class RemoteOpenstackSetJdkTest {
	private final Container root = RootContainer.builder().name("test-jdk-root").withFabric().build();
	private final Container ssh = SshContainer.builder().name("test-jdk-ssh").parent(root).build();
	private final Container rootChild = ChildContainer.builder().name("test-jdk-child").parent(root).build();
	private final Container sshChild = ChildContainer.builder().name("test-jdk-ssh-child").parent(ssh).build();

	@Rule
	public Fafram fafram = new Fafram().withFabric().provider(FaframProvider.OPENSTACK).containers(root, ssh, rootChild, sshChild).jdk(Openstack.JDK8);

	@BeforeClass
	public static void before() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:/home/fuse/storage/fuse/jboss-fuse-full-6.2.1.redhat-084.zip");
	}

	@After
	public void tearDown() {
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}

	@Test
	public void testSettingJavaHomeForSshContainer() throws Exception {
		log.debug(ssh.executeCommand("exec ps aux | grep " + ssh.getName()));
		log.debug(sshChild.executeCommand("exec ps aux | grep " + sshChild.getName()));
		log.debug(fafram.executeNodeCommand("ps aux | grep " + root.getName()));
		log.debug(fafram.executeNodeCommand("ps aux | grep " + rootChild.getName()));

		assertTrue(ssh.executeCommand("exec ps aux | grep " + ssh.getName()).contains(Openstack.JDK8.getPath()));
		assertTrue(sshChild.executeCommand("exec ps aux | grep " + sshChild.getName()).contains(Openstack.JDK8.getPath()));
		assertTrue(fafram.executeNodeCommand("ps aux | grep " + root.getName()).contains(Openstack.JDK8.getPath()));
		assertTrue(fafram.executeNodeCommand("ps aux | grep " + rootChild.getName()).contains(Openstack.JDK8.getPath()));
	}
}
