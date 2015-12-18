package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.cluster.ContainerBuilder;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/** Tests execution of commands right after Fuse start.
 * Created by mmelko on 30/11/15.
 */
public class CommandsTest {

	private static final String PROFILE = "test-profile";

	private ContainerBuilder containerBuilder = new ContainerBuilder();

	@ClassRule
	public static Fafram fafram = new Fafram().withFabric()
			.command("profile-create --parent default "+PROFILE)
			.getBuilder().child("child").addProfile(PROFILE).addToFafram().getFafram();

	@Test
	public void buildSmokeTest() {

		System.out.println(fafram.executeCommand("container-list"));
		Assert.assertTrue(fafram.executeCommand("container-list | grep child").contains("success"));
		Assert.assertTrue(fafram.executeCommand("container-list | grep child").contains(PROFILE));
	}
}
