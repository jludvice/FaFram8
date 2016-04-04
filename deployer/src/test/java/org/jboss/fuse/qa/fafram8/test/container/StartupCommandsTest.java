package org.jboss.fuse.qa.fafram8.test.container;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Test;

/**
 * Created by avano on 4.4.16.
 */
public class StartupCommandsTest {
	private Fafram fafram;

	@Test
	public void defaultContainerStartupCommandsTest() {
		fafram = new Fafram().commands("set DEBUG").setup();
		assertTrue(fafram.executeCommand("log:display").contains("DEBUG"));
	}

	@Test
	public void rootContainerStartupCommandsTest() {
		fafram = new Fafram().containers(RootContainer.builder().defaultRoot().commands("set DEBUG").build()).setup();
		assertTrue(fafram.executeCommand("log:display").contains("DEBUG"));
	}

	@After
	public void shutdown() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
