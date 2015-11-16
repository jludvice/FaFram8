package org.jboss.fuse.qa.fafram8.test.builder;

import org.jboss.fuse.qa.fafram8.cluster.Container;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Created by mmelko on 10/11/15.
 */
public class ContainerRootTest {
	@ClassRule
	public static Fafram fafram = new Fafram()
			.withFabric()
			.host("10.8.50.252").hostUser("fuse").hostPassword("fuse")
			.fuseZip("http://repository.jboss.org/nexus/content/repositories/ea/org/jboss/fuse/jboss-fuse-full/6.2.1.redhat-071/jboss-fuse-full-6.2.1.redhat-071.zip");

	@Test
	public void sshFaframTest(){
		fafram.getBuilder().root("admin","admin").name("root2")
				.nodeSsh("10.8.50.255","fuse","fuse")
				.addToFafram()

				.root("admin","admin")
				.name("ssh2")
				.nodeSsh("10.8.52.14")
				.addToFafram()
				.buildAll();

		for(Container c:fafram.getContainerList()){
			System.out.println(c.getHostNode());
		}

		//Assert.assertTrue(fafram.executeCommand("container-list | grep ssh2").contains("success"));

	}

}
