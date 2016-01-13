package org.jboss.fuse.qa.fafram8.test.builder;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests ability of builder/fafram to create root containers.
 * Created by mmelko on 10/11/15.
 */
@Slf4j
public class ContainerRootTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static final String ROOT_NAME = "Fafram-Test-root";
	public static final String ROOT2_NAME = "fafram-Test-secondroot";
	public static String ipRoot = "";
	public static String ipSsh = "";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(ROOT_NAME);
		osm.spawnNewServer(ROOT2_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(ROOT_NAME).getId());
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(ROOT2_NAME).getId());
		System.out.println("Machine " + ROOT_NAME + " spawned on " + ipRoot);
		System.out.println("Machine " + ROOT2_NAME + " spawned on " + ipSsh);
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
		System.setProperty(FaframConstant.HOST, ipRoot);
		Thread.sleep(60000);
	}

	public static Fafram fafram = new Fafram()
			.withFabric().host(ipRoot).name("root1")
			.hostUser("fuse").hostPassword("fuse");

	@Test
	public void sshFaframTest() {
		fafram.getBuilder().root("admin", "admin").name("root2")
				.nodeSsh(ipSsh, "fuse", "fuse", 22)
				.addToFafram();
		fafram.setup();

		Container root2 = null;
		for (Container c : fafram.getContainerList()) {
			System.out.println(c.getHostNode());
			if ("root2".equals(c.getName()))
				root2 = c;
		}

		Assert.assertTrue(fafram.executeCommand("container-list | grep root").contains("success"));
		assertEquals("karaf.name", "root1", fafram.executeCommand("system-property karaf.name").trim());
		Assert.assertTrue(root2.executeCommand("container-list | grep root").contains("success"));
		assertEquals("karaf.name", "root2", root2.executeCommand("system-property karaf.name").trim());
	}

	@AfterClass
	public static void after() {
		fafram.tearDown();
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
