package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
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

/**
 * Tests ability of builder/fafram to create child containers.
 * Created by mmelko on 03/11/15.
 */
public class ContainerBuilderChildsTest {

	private ContainerBuilder containerBuilder = new ContainerBuilder();
	private static OpenStackProvisionProvider osm = new OpenStackProvisionProvider();

	private static final String SERVER_NAME = "ContainerBuilderChildsTest";
	private static String ipRoot = "";

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
	public Fafram fafram = new Fafram().withFabric();

	@Test
	public void buildSmokeTest() {
		Container c = containerBuilder.child()
				.name("test-container")
				.addProfile("default")
				.addProfile("second-profile")
				.nodeSsh("test", "fuse", "fuse")
				.parent(new Container("root"))
				.build();
		Assert.assertNotNull(c);
		Assert.assertNotNull(c.getHostNode());
		Assert.assertNotNull(c.getContainerType());
	}

	@Test
	public void buildSeveralChildContainers() {

		Container c1, c2, c3;

		c1 = containerBuilder.child().name("child1").
				//setRootParent().
						parent(new Container("root")).
						build();
		Assert.assertEquals(c1.getName(), "child1");
		fafram.executeCommand("container-delete child1");

		c2 = containerBuilder.child().name("child2").
				//setRootParent().
						parent(new Container("root")).

						addProfile("default")
				.build();
		Assert.assertEquals(c2.getName(), "child2");
		fafram.executeCommand("container-delete child2");

		c3 = containerBuilder.child().name("child3").
				//.setRootParent()
						parent(new Container("root")).
						build();
		Assert.assertEquals(c3.getName(), "child3");
		fafram.executeCommand("container-delete child3");
	}

	@Test
	public void childContainersTest() {
		ContainerBuilder builder = fafram.getBuilder();

		builder.child().name("child1")
				.addToFafram()
				.buildAll();

		Assert.assertTrue(fafram.executeCommand("container-list | grep child1").contains("success"));
		fafram.getContainer("child1").delete();
		Assert.assertFalse(fafram.executeCommand("container-list | grep child1").contains("success"));
	}

	@After
	public void cleanUp() {
		fafram.executeCommand("container-delete child1");
		fafram.executeCommand("container-delete child2");
		fafram.executeCommand("container-delete child3");
	}

	@AfterClass
	public static void release() {
		osm.releaseResources();
	}
}
