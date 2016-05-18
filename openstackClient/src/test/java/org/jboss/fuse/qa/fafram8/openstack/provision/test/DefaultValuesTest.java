package org.jboss.fuse.qa.fafram8.openstack.provision.test;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.openstack.provision.OpenStackClient;

import org.junit.Test;

/**
 * OpenStack client default values test.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class DefaultValuesTest {
	private OpenStackClient osm;
	private String namePrefix = "openstack-client-test";
	private String serverName = "hello-kitty";
	private String imageId = "58487173-9c24-4644-97e7-f5f3e8d5e34e";

	@Test
	public void testOverridingValues() throws Exception {
		osm = OpenStackClient.builder().defaultOS7client().image(imageId).namePrefix(namePrefix).build();
		osm.spawnNewServer(serverName);
		final String id = osm.getServerFromRegister(namePrefix + "-" + serverName).getImageId();

		assertEquals("Image ID should be overwritten.", imageId, id);
	}
}
