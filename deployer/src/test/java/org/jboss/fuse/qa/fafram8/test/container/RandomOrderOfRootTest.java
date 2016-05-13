package org.jboss.fuse.qa.fafram8.test.container;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test random container ordering.
 * Created by mmelko on 12/05/16.
 */
public class RandomOrderOfRootTest {
	private static final String ROOT_NAME = "test-root";

	@Rule
	public Fafram fafram = new Fafram()
			.containers(
					ChildContainer.builder().name("child").parentName(ROOT_NAME).build(),
					RootContainer.builder().defaultRoot().withFabric().name(ROOT_NAME).build());

	@Test
	public void testIfSuccessfull() {
		final String res = fafram.executeCommand("container-list");
		Assert.assertTrue(res.contains(ROOT_NAME));
		Assert.assertTrue(res.contains("child"));
	}
}
