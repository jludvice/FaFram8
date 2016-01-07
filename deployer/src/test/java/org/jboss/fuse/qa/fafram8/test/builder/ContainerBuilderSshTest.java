package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests ability of builder/fafram to create ssh containers.
 * Created by mmelko on 06/11/15.
 */
@Slf4j
public class ContainerBuilderSshTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static final String SERVER_NAME = "FaframBuilderNode";
	public static final String SSH_NAME = "FaframBuilderSSH";
	public static String ipRoot = "";
	public static String ipSsh = "";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SERVER_NAME);
		osm.spawnNewServer(SSH_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(SERVER_NAME).getId());
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(SSH_NAME).getId());
		System.out.println("Machine " + SERVER_NAME + " spawned on " + ipRoot);
		System.out.println("Machine " + SSH_NAME + " spawned on " + ipSsh);
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
		System.setProperty(FaframConstant.HOST, ipRoot);
		Thread.sleep(60000);
	}

	public static Fafram fafram = new Fafram()
			.withFabric().host(ipRoot)
			.hostUser("fuse").hostPassword("fuse");

	@Test
	public void buildSshContainers() {
		Container c1, c2, c3;

		c1 = containerBuilder.ssh().name("ssh1").
				parent(new Container("root")).
				nodeSsh("host1", "fuse", "fuse").
				env("java_home=/blabal").

				build();

		c2 = containerBuilder.ssh().name("ssh2").
				parent(new Container("root")).
				addProfile("default")
				.nodeSsh("host2")
				.path("/hudson/static")
				.build();

		c3 = containerBuilder.ssh().name("ssh3").
				parent(new Container("root"))
				.nodeSsh("host3").
						path(null).
						env(null).
						build();

		System.out.println(c1.getContainerType().getCreateCommand() + "\n" +
				c2.getContainerType().getCreateCommand() + "\n" +
				c3.getContainerType().getCreateCommand() + "\n");

		Assert.assertEquals(c1.getName(), "ssh1");
		Assert.assertEquals(c2.getName(), "ssh2");
		Assert.assertEquals(c3.getName(), "ssh3");
	}

	@Test
	@Ignore("issue #39") // TODO(mmelko): investigate
	public void sshFaframTest() {
		fafram.setup();

		fafram.getBuilder().ssh("ssh-test")
				.nodeSsh(ipSsh, "fuse", "fuse")
				.addToFafram()
				.buildAll();

		Assert.assertTrue(fafram.executeCommand("container-list | grep ssh-test").contains("success"));
	}

	@AfterClass
	public static void after() {
		fafram.tearDown();
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
