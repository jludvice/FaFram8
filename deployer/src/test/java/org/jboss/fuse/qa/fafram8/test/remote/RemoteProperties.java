package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Remote adding/changing property.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteProperties {
	@Rule
	public Fafram fafram = new Fafram().modifyProperty("etc/custom.properties", "test.property", "testing", true)
			.modifyProperty("etc/custom.properties", "karaf.systemBundlesStartLevel", "60", false).suppressStart();

	@Test
	public void testAddingProperty() {
		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" +
				File.separator + "custom.properties").contains("test.property=testing"));
		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" +
				File.separator + "custom.properties").contains("karaf.systemBundlesStartLevel=60"));
	}
}
