package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 1.4.16.
 */
public class RemoveFromListTest {
	@Rule
	public Fafram fafram = new Fafram().containers(
			RootContainer.builder().defaultRoot().withFabric().build(),
			ChildContainer.builder().name("child").parentName("root").build()
	);

	@Test
	public void removeFromListTest() {
		assertEquals("Container list size", 2, ContainerManager.getContainerList().size());
		fafram.getContainer("child").destroy();
		assertEquals("Container list size", 1, ContainerManager.getContainerList().size());
	}
}
