package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

/** Tests execution of commands right after Fuse start.
 * Created by mmelko on 30/11/15.
 */
public class CommandsTest {

	private static final String PROFILE = "test-profile";

	private ContainerBuilder containerBuilder = new ContainerBuilder();
	public static String SERVER_NAME = "CommandsTest" + new Date().getTime();
	public static String ipRoot = "";

	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	@BeforeClass
	public static void before() throws InterruptedException {
		osm.spawnNewServer(SERVER_NAME);
		ipRoot = osm.assignFloatingAddress(osm.getServerByName(SERVER_NAME).getId());
		System.out.println("Machine " + SERVER_NAME + " spawned on " + ipRoot);
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_URL);
		System.setProperty(FaframConstant.HOST, ipRoot);
		Thread.sleep(60000);
	}

	@Rule
	public Fafram fafram = new Fafram().withFabric().host(ipRoot).hostUser("fuse").hostPassword("fuse").fuseZip(FaframTestBase
			.CURRENT_URL)
			.command("profile-create --parent default "+PROFILE)
			.getBuilder().child("child").addProfile(PROFILE).addToFafram().getFafram();

	@Test
	public void buildSmokeTest() {
		Assert.assertTrue(fafram.executeCommand("container-list | grep child").contains("success"));
		Assert.assertTrue(fafram.executeCommand("container-list | grep child").contains(PROFILE));
	}

	@After
	public void cleanUp() {
		fafram.executeCommand("container-delete child");
	}

	@AfterClass
	public static void release() {
		osm.releaseResources();
		System.clearProperty(FaframConstant.HOST);
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}
}
