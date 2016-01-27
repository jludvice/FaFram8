package org.jboss.fuse.qa.fafram8.test.configuration;

import static org.junit.Assert.assertEquals;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by ecervena on 1/11/16.
 */
public class ParserTest {

	private static Fafram fafram;

	@BeforeClass
	public static void init() {
		fafram = new Fafram().setConfigPath("src/test/resources/ParserTestConfiguration.xml");
		fafram.initConfiguration();
	}

	@Test
	public void parseRootContainerTest() {
		assertEquals("root1", Fafram.getContainerList().get(0).getName());
		assertEquals("fafram8-ssh-container-a", Fafram.getContainerList().get(1).getName());
		assertEquals("fafram8-ssh-container-b", Fafram.getContainerList().get(2).getName());
	}
}
