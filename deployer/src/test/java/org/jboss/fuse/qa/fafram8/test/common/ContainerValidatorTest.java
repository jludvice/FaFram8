package org.jboss.fuse.qa.fafram8.test.common;

import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Test;

/**
 * Container validator test.
 * Created by avano on 3.2.16.
 */
public class ContainerValidatorTest {
	private Fafram fafram;

	@Test(expected = ValidatorException.class)
	public void rootNoNodeTest() {
		fafram = new Fafram();
		fafram.containers(RootContainer.builder().name("noNode").build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void rootNoNameTest() {
		fafram = new Fafram();
		fafram.containers(RootContainer.builder().build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void rootInvalidNodeTest() {
		fafram = new Fafram();
		fafram.containers(RootContainer.builder().name("test").node(Node.builder().build()).build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void rootParentTest() {
		fafram = new Fafram();
		fafram.containers(RootContainer.builder().name("test").node(Node.builder().host("a").port(1).username("a").password("a").build()).build()
				.parentName("rootparent"));
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void childNoNameTest() {
		fafram = new Fafram();
		fafram.withFabric().containers(ChildContainer.builder().build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void childNonExistentParentTest() {
		fafram = new Fafram();
		fafram.withFabric().containers(ChildContainer.builder().name("test").parentName("nonexistent").build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void childNoParentTest() {
		fafram = new Fafram();
		fafram.withFabric().containers(ChildContainer.builder().name("test").build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void sshNoParentTest() {
		fafram = new Fafram();
		fafram.withFabric().containers(SshContainer.builder().name("test").node(Node.builder().host("a").port(1)
				.username("a").password("a").build()).build());
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void containersWithoutFabricTest() {
		fafram = new Fafram();
		fafram.containers(ChildContainer.builder().build());
		fafram.setup();
	}
}
