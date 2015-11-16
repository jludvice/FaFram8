package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by mmelko on 03/11/15.
 */
public class ContainerBuilderChildsTest {

	private ContainerBuilder containerBuilder = new ContainerBuilder();

	@ClassRule
	public static Fafram fafram = new Fafram().withFabric();

	@Test
	public void buildSmokeTest() {
		Container c = containerBuilder.child()
				.name("test-container")
				.addProfile("default")
				.addProfile("second-profile")
				.nodeSsh("test", "fuse", "fuse")
				.parent(new Container("root"))
				.build();

		Assert.assertNotNull(c);
		Assert.assertNotNull(c.getHostNode());
		Assert.assertNotNull(c.getContainerType());
		System.out.println(c.getContainerType().getCreateCommand());
		//System.out.println();
	}

	@Test
	public void buildSeveralChildContainers() {

		Container c1, c2, c3;

		c1 = containerBuilder.child().name("child1").
				//setRootParent().
						parent(new Container("root")).
				build();

		c2 = containerBuilder.child().name("child2").
				//setRootParent().
						parent(new Container("root")).

				addProfile("default")
				.build();

		c3 = containerBuilder.child().name("child3").
				//.setRootParent()
						parent(new Container("root")).
				build();

		Assert.assertEquals(c1.getName(), "child1");
		Assert.assertEquals(c2.getName(), "child2");
		Assert.assertEquals(c3.getName(), "child3");
	}

	@Test
	public void childContainersTest() {
		ContainerBuilder builder = fafram.getBuilder();

		builder.child().name("child1")
				.addToFafram()
				.child().name("child2")
				.addProfile("default")
				.addToFafram()
				.child().name("child3")
				.addToFafram()
				.buildAll();

		System.out.println(fafram.executeCommand("container-list"));
		Assert.assertTrue(fafram.executeCommand("container-list | grep child2").contains("success"));
	}
}
