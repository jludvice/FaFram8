package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by avano on 21.1.16.
 */
@Slf4j
public class RemoteRestart {
	@Rule
	public Fafram fafram = new Fafram();

	@Test
	public void restartTest() throws Exception {
		log.info("Sleeping for 120sec");
		Thread.sleep(120000L);
		assertTrue(fafram.executeCommand("shell:info | grep Uptime").contains("minutes"));
		fafram.restart();
		assertTrue(fafram.executeCommand("shell:info | grep Uptime").contains("seconds"));
	}
}
