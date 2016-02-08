package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 5.2.16.
 */
public class ContainersTest {
	@Rule
	public Fafram fafram = new Fafram().withFabric();

	@Test
	public void addContainerInTestTest() {
		fafram.containers(ChildContainer.builder().name("child").parentName("root").build());
		assertTrue(fafram.executeCommand("container-list | grep child").contains("success"));
	}
}
