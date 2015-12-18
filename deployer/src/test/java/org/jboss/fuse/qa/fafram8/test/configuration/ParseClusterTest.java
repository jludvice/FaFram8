package org.jboss.fuse.qa.fafram8.test.configuration;

import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**Tests for parsing container configuration.
 * Created by mmelko on 08/11/15.
 */
@Ignore
public class ParseClusterTest {
	private static Fafram fafram;

	@BeforeClass
	public static void init() {
		fafram = new Fafram().setConfigPath("src/test/resources/parser_test1.xml");
		fafram.initConfiguration();
	}

	@Test
	public void parseRootContainersTest(){

	}

	@Test
	public void parseChildContainerTest(){

	}

	@Test
	public void parseSshContainerTest(){

	}
}
