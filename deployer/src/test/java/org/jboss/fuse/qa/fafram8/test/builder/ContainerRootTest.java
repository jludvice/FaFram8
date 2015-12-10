package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by mmelko on 10/11/15.
 */
@Slf4j
public class ContainerRootTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static final String ROOT_NAME = "Fafram-Test-root";
	public static final String ROOT2_NAME = "fafram-Test-secondroot";
	public static String ipRoot = "";
	public static String ipSsh = "";

	// associated floating IP address in Openstack
	//public static String ipAddress;

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(ROOT_NAME);
		osm.spawnNewServer(ROOT2_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(ROOT_NAME).getId());
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(ROOT2_NAME).getId());
		log.info("Testing node on Openstack spawned on IP address " + ipRoot);
		System.setProperty(FaframConstant.FUSE_ZIP, "http://download.eng.bos.redhat.com/brewroot/repos/jb-fuse-6.2-build/latest/maven/org/jboss/fuse/jboss-fuse-full/6.2.0.redhat-133/jboss-fuse-full-6.2.0.redhat-133.zip");
		System.setProperty(FaframConstant.HOST, ipRoot);
		Thread.sleep(30000);
	}

	public static Fafram fafram = new Fafram()
			.withFabric().host(ipRoot)
			.hostUser("fuse").hostPassword("fuse");
	//	.fuseZip("http://repository.jboss.org/nexus/content/repositories/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-071/jboss-fuse-full-6.2.1.redhat-071.zip");

	@Test
	public void sshFaframTest() {
		fafram.setup();
		fafram.getBuilder().root("admin", "admin").name("root2")
				.nodeSsh(ipSsh, "fuse", "fuse")
				.addToFafram()
				.buildAll();

		Container root2 = null;
		for (Container c : fafram.getContainerList()) {
			System.out.println(c.getHostNode());
			if ("root2".equals(c.getName()))
				root2 = c;
		}

		Assert.assertTrue(fafram.executeCommand("container-list | grep root").contains("success"));
		Assert.assertTrue(root2.executeCommand("container-list | grep root").contains("success"));
	}

	@AfterClass
	public static void after() {
		fafram.tearDown();
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
