package org.jboss.fuse.qa.fafram8.test.container;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.util.Option;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mmelko on 17/03/16.
 */
public class ContainerBuildersTest {

	@Test
	public void testChildContainerBuilder() {
		Container template = ChildContainer.builder()
				.name("template")
				.profiles("gateway-mq")
				.commands("profile-create template")
				.build();
		Container child = ChildContainer.builder(template).name("child1").profiles("gateway-http").build();
		Container child2 = ChildContainer.builder(template).name("child2").build();

		//test if name hasn't changed
		Assert.assertEquals(template.getName(), "template");
		//test if new profile is added
		Assert.assertFalse(child2.getProfiles().contains("gateway-http"));
		Assert.assertTrue(child.getProfiles().contains("gateway-http"));

		child2 = ChildContainer.builder(child).name("child2").build();

		Assert.assertNotEquals(child.getName(), child2.getName());
		Assert.assertEquals(child2.getProfiles().size(), child.getProfiles().size());
	}

	@Test
	public void testSshContainerBuilder() {
		Container template = SshContainer.builder()
				.name("template")
				.node("blah", "admin", "admin")
				.profiles("gateway-mq")
				.build();

		Assert.assertNotNull(template.getNode());
		Container ssh = SshContainer.builder(template).name("root").commands("profile-create root1").build();

		Container ssh2 = SshContainer.builder(template).name("root2").profiles("gateway-http").build();

		Assert.assertEquals("template", template.getName());
		//test if new profile is added
		Assert.assertTrue(ssh.getProfiles().size() < ssh2.getProfiles().size());

		//copy again
		ssh2 = ChildContainer.builder(ssh).name("child2").build();

		Assert.assertNotEquals(ssh.getName(), ssh2.getName());
		//test if profiles are copied not just added
		Assert.assertEquals(ssh.getProfiles().size(), ssh2.getProfiles().size());
	}

	@Test
	public void testRootContainerBuilder() {
		Container template = RootContainer.builder()
				.name("template")
				.profiles("gateway-mq")
				.commands("profile-create template")
				.build();

		Container root = RootContainer.builder(template).name("root").profiles("gateway-http").commands("profile-create root1").build();
		Container root2 = RootContainer.builder(template).name("root2").build();

		//test if name hasn't changed
		Assert.assertEquals("template", template.getName());
		//test if profiles were changed properly
		Assert.assertFalse(template.getProfiles().contains("gateway-http"));
		Assert.assertFalse(root2.getProfiles().contains("gateway-http"));

		Assert.assertEquals(template.getProfiles().size(), root2.getProfiles().size());
		Assert.assertEquals(2,root.getOptions().get(Option.COMMANDS).size());
	}
}
