package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.Container;
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
import org.junit.Rule;
import org.junit.Test;

/**
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteMountStorageTest {
	ProvisionProvider provider = new OpenStackProvisionProvider();

	@Rule
	public Fafram fafram = new Fafram().name("mount-test").provideNodes(provider).withFabric().getBuilder().ssh("ssh-mount").nodeSsh("openstack", "fuse", "fuse").addToFafram().getFafram();

	@BeforeClass
	public static void before(){
		System.setProperty(FaframConstant.FUSE_ZIP, "/home/fuse/storage/fuse/jboss-fuse-full-6.2.1.redhat-084.zip");
	}

	@AfterClass
	public static void clean() {
		Fafram.getProvisionProvider().releaseResources();
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}

	@Test
	public void testMount() throws VerifyFalseException, SSHClientException, KarafSessionDownException {
		for(Container c : fafram.getContainerList()){
			SSHClient sshClient = new NodeSSHClient().defaultSSHPort().hostname(c.getHostNode().getHost())
					.username(c.getHostNode().getUsername()).password(c.getHostNode().getPassword());

			sshClient.connect(true);

			String response = sshClient.executeCommand("ls storage/", true);

			assertTrue(response.contains("fuse"));
		}
	}

}
