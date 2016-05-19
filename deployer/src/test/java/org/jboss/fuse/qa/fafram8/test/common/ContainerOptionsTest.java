package org.jboss.fuse.qa.fafram8.test.common;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.Rule;
import org.junit.Test;

/**
 * Created by avano on 19.5.16.
 */
public class ContainerOptionsTest {
	private Container root = RootContainer.builder().defaultRoot().withFabric().name("containeropts").build();
	private Container child = ChildContainer.builder().name("containeropts-child").options("--version 1.1").jvmOpts("-Dsomething=something")
			.parent(root).profiles("myprofile").build();
	private Container ssh = SshContainer.builder().name("containeropts-ssh").options("--version 1.1").jvmOpts("-Dsomething=something")
			.parent(root).profiles("myprofile").build();
	@Rule
	public Fafram fafram = new Fafram().provider(FaframProvider.OPENSTACK)
			.commands("version-create 1.1", "profile-create --version 1.1 myprofile")
			.containers(root, child, ssh)
			.fuseZip(FaframTestBase.CURRENT_LOCAL_URL);

	@Test
	public void containerOptsTest() {
		assertTrue(root.executeCommand("container-list | grep containeropts-child").contains("1.1"));
		assertTrue(root.executeCommand("container-list | grep containeropts-child").contains("myprofile"));
		assertTrue(root.executeCommand("container-list | grep containeropts-ssh").contains("1.1"));
		assertTrue(root.executeCommand("container-list | grep containeropts-ssh").contains("myprofile"));
	}
}
