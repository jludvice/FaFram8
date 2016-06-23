package org.jboss.fuse.qa.fafram8.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.resolver.Resolver;
import org.jboss.fuse.qa.fafram8.util.OptionUtils;

import org.junit.Test;

/**
 * Created by avano on 23.6.16.
 */
public class ContainerOptionsTest {
	private Container child = ChildContainer.builder()
			.profiles("profile1")
			.profiles("profile2") // both should be used
			.commands("echo 1")
			.commands("echo 2") // both should be used
			.resolver(Resolver.PUBLICHOSTNAME)
			.resolver(Resolver.LOCALHOSTNAME) // only this one should be used
			.version("1.1")
			.version("1.2") // only this one should be used
			.jvmOpts("Jvmopt1")
			.jvmOpts("Jvmopt2") // both should be used
			.build();

	@Test
	public void commandTest() {
		final String s = OptionUtils.getCommand(child.getOptions());
		assertTrue(s.contains("--version \"1.2\""));
		assertFalse(s.contains("1.1"));
		assertTrue(s.contains("--resolver \"localhostname\""));
		assertFalse(s.contains("--resolver \"publichostname\""));
		assertTrue(s.contains("--profile \"profile1\" --profile \"profile2\""));
		assertTrue(s.contains("--jvm-opts \"-Djava.security.egd=file:/dev/./urandom Jvmopt1 Jvmopt2\""));
		assertFalse(s.contains("echo"));
	}
}
