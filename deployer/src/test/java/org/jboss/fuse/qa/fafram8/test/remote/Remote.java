package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

/**
 * Basic remote test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class Remote {
	@Rule
	public Fafram fafram = new Fafram();

	@Test
	public void testBasicRemote() throws Exception {
		String response = fafram.executeCommand("osgi:list -t 0 | grep \"Apache Karaf :: Shell :: Console\"");
		assertNotNull(response);
		assertTrue(response.contains("Apache Karaf :: Shell :: Console"));
		assertTrue(response.contains("[Active"));
		assertTrue(response.contains("[Created"));
	}
}
