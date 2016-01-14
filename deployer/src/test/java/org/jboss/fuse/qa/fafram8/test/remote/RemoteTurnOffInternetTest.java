package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.Node;
import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.exceptions.VerifyFalseException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.ssh.NodeSSHClient;
import org.jboss.fuse.qa.fafram8.ssh.SSHClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for setting up offline environment when using OpenStack provision.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteTurnOffInternetTest {
	private ProvisionProvider provider = new OpenStackProvisionProvider();

	@Rule
	public Fafram fafram = new Fafram().name("internet-test").provideNodes(provider).withFabric().getBuilder().ssh("ssh-internet").nodeSsh("openstack", "fuse", "fuse").addToFafram().getFafram().offline();

	@BeforeClass
	public static void before() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:/home/fuse/storage/fuse/jboss-fuse-full-6.2.1.redhat-084.zip");
	}

	@AfterClass
	public static void clean() {
		Fafram.getProvisionProvider().releaseResources();
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}

	//TODO(rjakubco): uncomment this when OpenStack is more stable and fafram was refactored
	@Test
	@Ignore
	public void testInternet() throws VerifyFalseException, SSHClientException, KarafSessionDownException {
		Node rootNode = fafram.getContainerList().get(0).getHostNode();
		for (Container c : fafram.getContainerList()) {
			String preCommand = "";
			if (!c.isRoot()) {
				preCommand = "ssh -o StrictHostKeyChecking=no " + c.getHostNode().getUsername() + "@" + c.getHostNode().getHost() + " ";
			}

			SSHClient sshClient = new NodeSSHClient().defaultSSHPort().hostname(rootNode.getHost())
					.username(rootNode.getUsername()).password(rootNode.getPassword());

			sshClient.connect(true);

			String response = sshClient.executeCommand(preCommand + "wget www.google.com", true);

			assertTrue(response.contains("failed: Connection refused") && response.contains("failed: Network is unreachable."));
		}
	}
}
