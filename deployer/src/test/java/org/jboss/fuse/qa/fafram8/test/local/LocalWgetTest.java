package org.jboss.fuse.qa.fafram8.test.local;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Local downloading of Fuse from http via wget test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class LocalWgetTest {
	private Fafram fafram;

	@Before
	public void init() {
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
	}

	@Test
	public void testWgetZip() throws Exception {
		fafram = new Fafram();
		fafram.setup();
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
