package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.provision.provider.ProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Uncoment "ConfigurationParser.setDeployer();" in Fafram setup method to enable remote deployment.
 *
 * Created by ecervena on 25.9.15.
 */
public class OpenStackProvisionTest {

	ProvisionProvider provider = new OpenStackProvisionProvider();
	@Rule
	public Fafram fafram = new Fafram().provideNodes(provider).withFabric();

	@BeforeClass
	public static void init() {
		System.setProperty(FaframConstant.KEEP_OS_RESOURCES, "false");
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
	}

	@AfterClass
	public static void clean() {
		Fafram.getProvisionProvider().releaseResources();
	}

	//Uncomment ConfigurationParser.setDeployer(); in Fafaram to run remote deployment
	@Ignore
	@Test
	public void fabricTest() {
		assertTrue(fafram.executeCommand("container-list | grep root").contains("success"));
		assertTrue(fafram.executeCommand("container-list | grep node1").contains("success"));
	}
}
