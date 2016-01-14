package org.jboss.fuse.qa.fafram8.test.provision;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.provision.provider.OpenStackProvisionProvider;

import org.junit.AfterClass;
import org.junit.Test;

import org.openstack4j.model.compute.Server;

/**
 * Created by ecervena on 24.9.15.
 */
public class SpawnNodeFromSnapshotTest {

	@Test
	public void spawnNewNodeTest() {
		OpenStackProvisionProvider osm = new OpenStackProvisionProvider();
		osm.spawnNewServer("hello-kitty");
		assertEquals("Wrong name returned.", "fafram8-hello-kitty", osm.getServerByName("fafram8-hello-kitty").getName());
	}

	@AfterClass
	public static void clean() {
		OpenStackProvisionProvider osm = new OpenStackProvisionProvider();
		Server server = osm.getServerByName("fafram8-hello-kitty");
		osm.getOs().compute().servers().delete(server.getId());
		System.out.println("Instance " + server.getName() + " with ID " + server.getId() + " has been deleted.");
	}
}
