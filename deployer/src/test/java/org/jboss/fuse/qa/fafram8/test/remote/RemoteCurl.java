package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO(rjakubco): once the other tests are in file: protocol then this test will make sense.
 * Remote downloading of Fuse from http via curl test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteCurl {
	private Fafram fafram;

	@Before
	public void init() {
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
	}

	@Test
	public void testCurlZip() throws Exception {
		fafram = new Fafram();
		fafram.setup();

		String response = fafram.executeCommand("osgi:list -t 0 | grep \"Apache Karaf :: Shell :: Console\"");
		assertNotNull(response);
		assertTrue(response.contains("Apache Karaf :: Shell :: Console"));
		assertTrue(response.contains("[Active"));
		assertTrue(response.contains("[Created"));
	}

	@After
	public void tearDown() throws Exception {
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
