package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests ability of builder/fafram to create ssh containers.
 * Created by mmelko on 06/11/15.
 */
@Slf4j
public class ContainerBuilderSshTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static final String SERVER_NAME = "FaframBuilderNode" + new Date().getTime();
	public static final String SSH_NAME = "FaframBuilderSSH" + new Date().getTime();
	public static String ipRoot = "";
	public static String ipSsh = "";
	public static final String OPENSTACK_NAME_PREFIX = "fafram8";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		System.setProperty(FaframConstant.OPENSTACK_NAME_PREFIX, OPENSTACK_NAME_PREFIX);
		log.info("Spawning testing node...");
		osm.spawnNewServer(SERVER_NAME);
		osm.spawnNewServer(SSH_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(OPENSTACK_NAME_PREFIX + "-" + SERVER_NAME).getId());
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(OPENSTACK_NAME_PREFIX + "-" + SSH_NAME).getId());
		log.info("Testing node on Openstack spawned on IP address " + ipRoot);
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);		System.setProperty(FaframConstant.HOST, ipRoot);
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
	public void sshFaframTest() {
		fafram.setup();

		fafram.getContainerBuilder().ssh("ssh-test")
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
