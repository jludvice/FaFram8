package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 5.2.16.
 */
public class ContainersTest {
	@Rule
	public Fafram fafram = new Fafram().provider(FaframProvider.OPENSTACK).fuseZip(FaframTestBase.CURRENT_URL).containers(
			RootContainer.builder().defaultRoot().name("test-root").build()
	);

	@Test
	public void addContainerInTestTest() {
		final Container root2 = RootContainer.builder().name("root2").build();
		fafram.containers(root2);
		assertEquals("Root2 response", "1", root2.executeCommand("echo 1"));
	}
}
