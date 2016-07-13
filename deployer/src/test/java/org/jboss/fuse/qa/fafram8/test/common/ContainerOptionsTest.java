package org.jboss.fuse.qa.fafram8.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.resolver.Resolver;
import org.jboss.fuse.qa.fafram8.util.OptionUtils;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by avano on 23.6.16.
 */
@Slf4j
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
			.bindAddress("1.2.3.4")
			.bindAddress("2.3.4.5") // only this should be used
			.datastore("first", "second")
			.datastore("third") // all should be used
			.jmxUser("u1")
			.jmxUser("u2") // only this should be used
			.jmxPassword("p1")
			.jmxPassword("p2") // only this should be used
			.manualIp("ip1")
			.manualIp("ip2") // only this should be used
			.zookeeperPassword("pass1")
			.zookeeperPassword("pass2") // only this should be used
			.build();

	private Container ssh = SshContainer.builder()
			.sshRetries(20)
			.sshRetries(30)
			.disableDistributionUpload()
			.fallbackRepos("repo1", "repo2")
			.fallbackRepos("repo3")
			.passPhrase("p1")
			.passPhrase("p2")
			.port(23)
			.port(24)
			.privateKey("key1")
			.privateKey("key2")
			.proxyUri("uri1")
			.proxyUri("uri2")
			.withAdminAccess()
			.build();
	@Test
	public void commandTest() {
		String s = OptionUtils.getCommand(child.getOptions());
		assertTrue(s.contains("--version \"1.2\""));
		assertFalse(s.contains("1.1"));
		assertTrue(s.contains("--resolver \"localhostname\""));
		assertFalse(s.contains("--resolver \"publichostname\""));
		assertTrue(s.contains("--profile \"profile1\" --profile \"profile2\""));
		assertTrue(s.contains("--jvm-opts \"-Djava.security.egd=file:/dev/./urandom Jvmopt1 Jvmopt2\""));
		assertFalse(s.contains("echo"));
		assertTrue(s.contains("--bind-address \"2.3.4.5\""));
		assertFalse(s.contains("--bind-address \"1.2.3.4\""));
		assertTrue(s.contains("--datastore-option \"first\""));
		assertTrue(s.contains("--datastore-option \"second\""));
		assertTrue(s.contains("--datastore-option \"third\""));
		assertTrue(s.contains("--manual-ip \"ip2\""));
		assertFalse(s.contains("--manual-ip \"ip1\""));

		s = OptionUtils.getCommand(ssh.getOptions());
		assertTrue(s.contains("--ssh-retries \"30\""));
		assertFalse(s.contains("--ssh-retries \"20\""));
		assertTrue(s.contains("--disable-distribution-upload"));
		assertTrue(s.contains("--fallback-repos \"repo1\""));
		assertTrue(s.contains("--fallback-repos \"repo2\""));
		assertTrue(s.contains("--fallback-repos \"repo3\""));
		assertTrue(s.contains("--pass-phrase \"p2\""));
		assertFalse(s.contains("--pass-phrase \"p1\""));
		assertTrue(s.contains("--port \"24\""));
		assertFalse(s.contains("--port \"23\""));
		assertTrue(s.contains("--private-key \"key2\""));
		assertFalse(s.contains("--private-key \"key1\""));
		assertTrue(s.contains("--proxy-uri \"uri2\""));
		assertFalse(s.contains("--proxy-uri \"uri1\""));
		assertTrue(s.contains("--with-admin-access"));
	}
}
