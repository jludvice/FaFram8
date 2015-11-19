package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Created by mmelko on 06/11/15.
 */
public class ContainerBuilderSshTest {
	private ContainerBuilder containerBuilder = new ContainerBuilder();

	@ClassRule
	public static Fafram fafram = new Fafram()
			.withFabric()
			.host("10.8.53.252").hostUser("fuse").hostPassword("fuse")
			.fuseZip("http://repository.jboss.org/nexus/content/repositories/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-071/jboss-fuse-full-6.2.1.redhat-071.zip");

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
		fafram.getBuilder().ssh().name("ssh1")
				.nodeSsh("10.8.49.255", "fuse", "fuse")
				.addToFafram()
				.ssh().name("ssh2").nodeSsh("10.8.52.14").addToFafram()
				.buildAll();

		Assert.assertTrue(fafram.executeCommand("container-list | grep ssh2").contains("success"));
	}
}
