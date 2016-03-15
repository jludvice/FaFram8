package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Remote add user test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteAddUser {

	private Fafram fafram;

	@Test
	public void testAddUser() {
		fafram = new Fafram().addUser("testu", "testp", "testr1,testr2").suppressStart();
		fafram.setup();
		assertTrue(fafram.executeNodeCommand("cat " + SystemProperty.getFusePath() + File.separator + "etc"
				+ File.separator + "users.properties").contains("testu=testp,testr1,testr2"));
	}

	@Test
	public void testOverrideUser() {
		fafram = new Fafram().addUser("fafram", "faframoverride", "Monitor").suppressStart();
		fafram.setup();
		assertTrue(fafram.executeNodeCommand("cat " + SystemProperty.getFusePath() + File.separator + "etc"
				+ File.separator + "users.properties").contains("fafram=faframoverride,Monitor"));
		assertFalse(fafram.executeNodeCommand("cat " + SystemProperty.getFusePath() + File.separator + "etc"
				+ File.separator + "users.properties").contains("fafram=fafram,Administrator"));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
