package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.exceptions.VerifyFalseException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for loading custom iptables configuration file from local to remote machine.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteSettingIPtablesTest {
	private ProvisionProvider provider = new OpenStackProvisionProvider();
	private final static String DIR = "test/working/directory";

//	@Rule
//	public Fafram fafram = new Fafram().name("iptables-test").provider(provider).withFabric();//.getContainerBuilder().ssh("ssh-iptables")
//	.nodeSsh("openstack", "fuse", "fuse").addToFafram().getFafram().loadIPtablesConfigurationFile("target/test-classes/iptables-test");

	@BeforeClass
	public static void before() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:/home/fuse/storage/fuse/jboss-fuse-full-6.2.1.redhat-084.zip");
	}

	@After
	public void tearDown() {
//		Fafram.getProvisionProvider().releaseResources();
		System.clearProperty(FaframConstant.WORKING_DIRECTORY);
	}

	@AfterClass
	public static void clean() {
		System.clearProperty(FaframConstant.FUSE_ZIP);
//		Fafram.getProvisionProvider().releaseResources();
	}

	//TODO(rjakubco): uncomment this when OpenStack is more stable and fafram was refactored
	@Test
	@Ignore
	public void testLoadingIptables() throws VerifyFalseException, SSHClientException, KarafSessionDownException {
//		Node rootNode = fafram.getContainerList().get(0).getNode();
//		for (Container c : fafram.getContainerList()) {
//			String preCommand = "";
//			if (!c.isRoot()) {
//				preCommand = "ssh -o StrictHostKeyChecking=no " + c.getNode().getUsername() + "@" + c.getNode().getHost() + " ";
//			}
//
//			SSHClient sshClient = new NodeSSHClient().defaultSSHPort().hostname(rootNode.getHost())
//					.username(rootNode.getUsername()).password(rootNode.getPassword());
//			sshClient.connect(true);
//
//			String response = sshClient.executeCommand(preCommand + "sudo iptables -L -n", true);
//
//			assertTrue(response.contains("ACCEPT     icmp --  0.0.0.0/0") && response.contains("state NEW tcp dpt:22"));
//		}
	}
}
