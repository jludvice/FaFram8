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
 * Created by mmelko on 06/11/15.
 */
@Slf4j
public class ContainerBuilderSshTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static final String SERVER_NAME = "FaframBuilderNode";
	public static final String SSH_NAME = "FaframBuilderSSH";
	public static String ipRoot="";
	public static String ipSsh="";

	// associated floating IP address in Openstack
	//public static String ipAddress;

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SERVER_NAME);
		osm.spawnNewServer(SSH_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(SERVER_NAME).getId());
		ipSsh = osm.assignFloatingAddress(osm.getServerByName(SSH_NAME).getId());
		log.info("Testing node on Openstack spawned on IP address " + ipRoot);
		System.setProperty(FaframConstant.FUSE_ZIP, "http://download.eng.bos.redhat.com/brewroot/repos/jb-fuse-6.2-build/latest/maven/org/jboss/fuse/jboss-fuse-full/6.2.0.redhat-133/jboss-fuse-full-6.2.0.redhat-133.zip");
		System.setProperty(FaframConstant.HOST,ipRoot);
		Thread.sleep(30000);
	}

	//@ClassRule
	public static Fafram fafram = new Fafram()
			.withFabric().host(ipRoot)
			.hostUser("fuse").hostPassword("fuse");
		//	.fuseZip("http://repository.jboss.org/nexus/content/repositories/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-071/jboss-fuse-full-6.2.1.redhat-071.zip");

	@Test
	public void buildSshContainers() {

		Container c1, c2, c3;

		c1 = containerBuilder.ssh().name("ssh1").
				parent(new Container("root")).
				nodeSsh("host1", "fuse", "fuse").
				env("java_home=/blabal").

				build();

		c2 = containerBuilder.ssh().name("ssh2").
				//setRootParent().
						parent(new Container("root")).
						addProfile("default")
				.nodeSsh("host2")
				.path("/hudson/static")
				.build();

		c3 = containerBuilder.ssh().name("ssh3").
				//.setRootParent()
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

		fafram.getBuilder().ssh("ssh-test")
				.nodeSsh(ipSsh, "fuse", "fuse")
				.addToFafram()
				.buildAll();

		Assert.assertTrue(fafram.executeCommand("container-list | grep ssh-test").contains("success"));
	}

	@AfterClass
	public static void after() {
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
