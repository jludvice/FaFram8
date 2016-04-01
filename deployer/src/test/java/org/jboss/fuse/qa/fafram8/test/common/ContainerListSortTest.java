package org.jboss.fuse.qa.fafram8.test.common;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created by avano on 31.3.16.
 */
public class ContainerListSortTest {
	private Container root = RootContainer.builder().defaultRoot().build(); // 0
	private Container childContainer = ChildContainer.builder().parent(root).build(); // 1
	private Container ssh = SshContainer.builder().parent(root).build(); // 1
	private Container sshChild = ChildContainer.builder().parent(ssh).build(); // 2
	private Container sshChilChild = ChildContainer.builder().parent(sshChild).build(); // 3
	private Container rootChildChild = ChildContainer.builder().parent(childContainer).build(); // 2
	private Container[] cArray = new Container[] {rootChildChild, sshChild, childContainer, root, sshChilChild, ssh};

	@Test
	public void containerSortTest() {
		Arrays.sort(cArray);
		assertTrue("Root expected on pos 0", cArray[0].equals(root));
		assertTrue("Ssh or ChildContainer expected on pos 1", cArray[1].equals(ssh) || cArray[1].equals(childContainer));
		assertTrue("Ssh or ChildContainer expected on pos 2", cArray[2].equals(ssh) || cArray[2].equals(childContainer));
		assertTrue("SshChild or RootChildChild expected on pos 3", cArray[3].equals(sshChild) || cArray[3].equals(rootChildChild));
		assertTrue("SshChild or RootChildChild expected on pos 4", cArray[4].equals(sshChild) || cArray[4].equals(rootChildChild));
		assertTrue("SshChildChild expected on pos 5", cArray[5].equals(sshChilChild));
	}
}
