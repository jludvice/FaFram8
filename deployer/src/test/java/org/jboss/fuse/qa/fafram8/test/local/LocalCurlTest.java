package org.jboss.fuse.qa.fafram8.test.local;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Local downloading of Fuse from http via curl test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class LocalCurlTest {
	private Fafram fafram;

	@Before
	public void init() {
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_HTTP_URL);
	}

	@Test
	public void testCurlZip() throws Exception {
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
