package org.jboss.fuse.qa.fafram8.test.remote;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for uploading bundle to fabric maven proxy.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteBundleUpload {
	@Rule
	public Fafram fafram = new Fafram().withFabric().bundle("src/test/resources/blank-project/pom.xml");

	@Test
	public void testUploadBundle() {
		fafram.executeCommand("osgi:install mvn:org.jboss.fuse.qa.test/useless-artifact/1.0");
		String response = fafram.executeCommand("list | grep useless");
		Assert.assertFalse(response.isEmpty());
		Assert.assertTrue(response.contains("useless-artifact (1.0.0)"));
	}
}
