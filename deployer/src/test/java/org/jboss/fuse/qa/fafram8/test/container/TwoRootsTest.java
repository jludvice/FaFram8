package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 5.2.16.
 */
public class TwoRootsTest {
	private static final Container TEMPLATE = RootContainer.builder()
			.node("openstack")
			.profiles("gateway-mq")
			.commands("profile-create template")
			.withFabric()
			.build();
	private Container root1 = RootContainer.builder(TEMPLATE).name("root").commands("profile-create root1").build();
	private Container root2 = RootContainer.builder(TEMPLATE).name("root2").profiles("gateway-http").build();
	@Rule
	public Fafram fafram = new Fafram().fuseZip(FaframTestBase.CURRENT_URL).provider(FaframProvider.OPENSTACK)
			.containers(root1, root2);

	@Test
	public void twoRootsTest() {
		assertTrue(root1.executeCommand("container-list").contains("success"));
		assertTrue(root1.executeCommand("container-list").contains("gateway-mq"));
		assertTrue(root1.executeCommand("profile-list").contains("template"));
		assertTrue(root1.executeCommand("profile-list").contains("root1"));
		assertTrue(root2.executeCommand("container-list").contains("success"));
		assertTrue(root2.executeCommand("container-list").contains("gateway-http"));
		assertTrue(root2.executeCommand("container-list").contains("gateway-mq"));
		assertTrue(root2.executeCommand("profile-list").contains("template"));
	}
}
