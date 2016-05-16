package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * Remote adding/changing property.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@Slf4j
public class RemoteProperties {
	@Rule
	public Fafram fafram = new Fafram()
			.modifyProperty("etc/custom.properties", "test.property", "testing", false)
			.modifyProperty("etc/custom.properties", "karaf.systemBundlesStartLevel", "60", false)
			.modifyProperty("etc/custom.properties", "karaf.startup.message", "Adding this string", true)
			.modifyProperty("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.repositories", "org.test.repository", false)
			.modifyProperty("etc/file.doesnt.exist.properties", "new.property.key", "new.property.value", false)
			.suppressStart();

	@Test
	public void testAddingProperty() {
		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "custom.properties")
				.contains("test.property=testing"));

		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "custom.properties")
				.contains("karaf.systemBundlesStartLevel=60"));

		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "custom.properties")
				.contains("Please wait while JBoss Fuse is loading...Adding this string"));

		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "org.ops4j.pax.url.mvn.cfg")
				.contains("org.ops4j.pax.url.mvn.repositories=org.test.repository"));
		// This default value present in Fuse. It shouldn't be there anymore
		assertFalse(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "org.ops4j.pax.url.mvn.cfg")
				.contains("http://repository.jboss.org/"));

		assertTrue(fafram.executeNodeCommand("cat " + fafram.getProductPath() + File.separator + "etc" + File.separator + "file.doesnt.exist.properties")
				.contains("new.property.key=new.property.value"));
	}
}
