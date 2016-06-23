package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.util.Option;

import org.junit.After;
import org.junit.Test;

/**
 * Created by avano on 16.2.16.
 */
public class FabricTest {
	private Fafram fafram;

	@Test
	public void faframWithFabricDefaultContainerTest() {
		fafram = new Fafram().withFabric("something").suppressStart();
		fafram.setup();

		assertTrue("Container does not have the fabric flag", ContainerManager.getContainer("root").isFabric());
		assertEquals("Fabric arguments size", 1, ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).size());
		assertEquals("Fabric arguments", "something", ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).get(0));
	}

	@Test
	public void defaultRootWithFabricTest() {
		fafram = new Fafram().containers(
				RootContainer.builder().defaultRoot().withFabric("something").build()
		).suppressStart();

		fafram.setup();

		assertTrue("Container does not have the fabric flag", ContainerManager.getContainer("root").isFabric());
		assertEquals("Fabric arguments size", 1, ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).size());
		assertEquals("Fabric arguments", "something", ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).get(0));
	}

	@Test
	public void faframWithFabricTwoContainersTest() {
		fafram = new Fafram().containers(
				RootContainer.builder().withFabric("something").defaultRoot().build(),
				RootContainer.builder().withFabric("something2").defaultRoot().name("root2").build()
		).suppressStart();

		fafram.setup();

		assertTrue("Container root does not have the fabric flag", ContainerManager.getContainer("root").isFabric());
		assertTrue("Container root2 does not have the fabric flag", ContainerManager.getContainer("root2").isFabric());
		assertEquals("Fabric arguments root size", 1, ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).size());
		assertEquals("Fabric arguments root2 size", 1, ContainerManager.getContainer("root2").getOptions().get(Option.FABRIC_CREATE).size());
		assertEquals("Fabric arguments root", "something", ContainerManager.getContainer("root").getOptions().get(Option.FABRIC_CREATE).get(0));
		assertEquals("Fabric arguments root2", "something2", ContainerManager.getContainer("root2").getOptions().get(Option.FABRIC_CREATE).get(0));
	}

	@After
	public void after() {
		fafram.tearDown();
	}
}
