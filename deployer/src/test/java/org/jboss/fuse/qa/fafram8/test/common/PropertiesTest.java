package org.jboss.fuse.qa.fafram8.test.common;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test if the system properties is returned instead of external property when the system property
 * is defined.
 * Created by avano on 4.2.16.
 */
public class PropertiesTest {
	static {
		System.setProperty(FaframConstant.OPENSTACK_NAME_PREFIX, "myPrefix");
	}

	@Rule
	public Fafram fafram = new Fafram().suppressStart();

	@Test
	public void propertyTest() {
		// Overriden property
		assertEquals("Property", "myPrefix",
				SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_NAME_PREFIX));
		// Default property
		assertEquals("Property", "fuseqe-lab", SystemProperty.getExternalProperty(FaframConstant.OPENSTACK_KEYPAIR));
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(FaframConstant.OPENSTACK_NAME_PREFIX);
	}
}
