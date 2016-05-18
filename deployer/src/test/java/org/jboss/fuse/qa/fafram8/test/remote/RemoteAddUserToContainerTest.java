package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 18.5.16.
 */
public class RemoteAddUserToContainerTest {
	private Container root1 = RootContainer.builder().defaultRoot().name("adduserroot1").addUser("myuser", "myuser", "admin").build();
	private Container root2 = RootContainer.builder().defaultRoot().name("adduserroot2").build();

	@Rule
	public Fafram fafram = new Fafram().provider(FaframProvider.OPENSTACK).suppressStart().containers(root1, root2).fuseZip(FaframTestBase.CURRENT_LOCAL_URL);

	@Test
	public void addUserTest() {
		assertTrue(root1.executeNodeCommand("cat " + root1.getFusePath() + "/etc/users.properties").contains("myuser=myuser,admin"));
		assertEquals("User", "myuser", root1.getExecutor().getClient().getUsername());
		assertEquals("Password", "myuser", root1.getExecutor().getClient().getPassword());
		assertFalse(root2.executeNodeCommand("cat " + root2.getFusePath() + "/etc/users.properties").contains("myuser=myuser,admin"));
		assertNotEquals("User", "myuser", root2.getExecutor().getClient().getUsername());
		assertNotEquals("Password", "myuser", root2.getExecutor().getClient().getPassword());
	}
}
