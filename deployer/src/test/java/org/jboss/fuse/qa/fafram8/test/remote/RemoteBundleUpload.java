package org.jboss.fuse.qa.fafram8.test.remote;

import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for uploading bundle to fabric maven proxy.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteBundleUpload {
	private Fafram fafram;

	@Test
	public void testUploadBundle() {
		fafram = new Fafram().withFabric().bundles("src/test/resources/blank-project/pom.xml").setup();
		fafram.executeCommand("osgi:install mvn:org.jboss.fuse.qa.test/useless-artifact/1.0");
		final String response = fafram.executeCommand("list | grep useless");
		Assert.assertFalse(response.isEmpty());
		Assert.assertTrue(response.contains("useless-artifact (1.0.0)"));
	}

	@Test
	public void testUploadBundleFromTest() throws Exception {
		fafram = new Fafram().withFabric().setup();
		((RootContainer) fafram.getContainer("root")).uploadBundles("src/test/resources/blank-project/pom.xml");
		fafram.executeCommand("osgi:install mvn:org.jboss.fuse.qa.test/useless-artifact/1.0");
		final String response = fafram.executeCommand("list | grep useless");
		Assert.assertFalse(response.isEmpty());
		Assert.assertTrue(response.contains("useless-artifact (1.0.0)"));
	}

	@After
	public void tearDown() throws Exception {
		fafram.tearDown();
		fafram.executeNodeCommand("rm -rf ~/.m2/repository");
	}
}
