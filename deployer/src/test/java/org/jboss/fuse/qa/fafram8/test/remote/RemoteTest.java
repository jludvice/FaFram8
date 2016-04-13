package org.jboss.fuse.qa.fafram8.test.remote;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Date;

import lombok.Getter;
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
		RemoteFaframJvmMemOpts.class,
		RemoteOnlyConnect.class,
		RemoteProperties.class,
		RemoteReplaceFile.class,
		RemoteRootName.class,
		RemoteRestart.class,
		Remote.class,
		RemoteCurl.class,
		RemoteBundleUpload.class,
		RemoteSetJdk.class,
		RemoteKillingContainers.class,
		RemoteExecuteNodeCommand.class
})
@Slf4j
public final class RemoteTest {
	public static final String SERVER_NAME = "build-FaframRemoteTestNode" + new Date().getTime();

	// associated floating IP address in Openstack
	@Getter
	private static String ipAddress;

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	private RemoteTest() {
	}

	@BeforeClass
	public static void before() throws InterruptedException {
		log.info("Spawning testing node...");
		osm.spawnNewServer(SERVER_NAME);

		ipAddress = osm.assignFloatingAddress(osm.getServerByName(SERVER_NAME).getId());

		log.info("Testing node on Openstack spawned on IP address " + ipAddress);

		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
		System.setProperty(FaframConstant.HOST, ipAddress);

		Thread.sleep(30000);
	}

	@AfterClass
	public static void after() {
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
