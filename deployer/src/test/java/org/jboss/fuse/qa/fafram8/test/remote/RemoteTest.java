package org.jboss.fuse.qa.fafram8.test.remote;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.openstack.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import lombok.extern.slf4j.Slf4j;

/**
 * Test suite for executing remote tests on only one provisioned machine in Openstack.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		RemoteAddUser.class,
		RemoteFabric.class,
		RemoteJvmOpts.class,
		RemoteOnlyConnect.class,
		RemoteProperties.class,
		RemoteReplaceFile.class,
		RemoteRootName.class,
		Remote.class,
		RemoteWget.class,
		RemoteWorkingDirectory.class
})
@Slf4j
public class RemoteTest {
	public static final String SERVER_NAME = "FaframRemoteTestNode";

	// associated floating IP address in Openstack
	public static String ipAddress;

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SERVER_NAME);

		ipAddress = osm.assignFloatingAddress(osm.getServerByName(SERVER_NAME).getId());

		log.info("Testing node on Openstack spawned on IP address " + ipAddress);

		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
		System.setProperty(FaframConstant.HOST, RemoteTest.ipAddress);

		Thread.sleep(30000);
	}

	@AfterClass
	public static void after() {
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
