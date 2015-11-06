package org.jboss.fuse.qa.fafram8.test.configuration;

import org.jboss.fuse.qa.fafram8.configuration.ConfigurationParser;
import org.jboss.fuse.qa.fafram8.property.SystemProperty;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

/**
 * Created by mmelko on 14/10/15.
 */
public class ParseFrameworkConfigTest {

	private static ConfigurationParser parser;
	private static Fafram fafram;

	@BeforeClass
	public static void init() {
		fafram = new Fafram().setConfigPath("src/test/resources/parser_test1.xml");
		fafram.initConfiguration();
	}

	@Ignore
	@Test
	public void parseFuseGroup() throws IOException, SAXException, ParserConfigurationException {
		Assert.assertEquals("testgroup", SystemProperty.getInstance().getFuseGroup());
	}

	@Ignore
	@Test
	public void fuseId() {
		Assert.assertEquals("testid", SystemProperty.getInstance().getFuseId());
	}

	@Ignore
	@Test
	public void fuseVersion() {
		Assert.assertEquals("testversion", SystemProperty.getInstance().getFuseVersion());
	}

	@Test
	public void fuseZip() {
		Assert.assertEquals("testzip", SystemProperty.getInstance().getFuseZip());
	}

	@Test
	public void fusePath() {
		Assert.assertEquals("testpath", SystemProperty.getInstance().getFusePath());
	}

	@Test
	public void startWaitTime() {
		Assert.assertEquals(999, SystemProperty.getInstance().getStartWaitTime());
	}

	@Test
	public void stopWaitTime() {
		Assert.assertEquals(999, SystemProperty.getInstance().getStopWaitTime());
	}

	@Test
	public void provisionWaitTime() {
		Assert.assertEquals(999, SystemProperty.getInstance().getProvisionWaitTime());
	}

	@Test
	public void patchWaitTime() {
		Assert.assertEquals(999, SystemProperty.getInstance().getPatchWaitTime());
	}

	@Test
	public void patch() {
		Assert.assertEquals("testpatch", SystemProperty.getInstance().getPatch());
	}

	@Test
	public void fabric() {
		Assert.assertEquals("testfabric", SystemProperty.getInstance().getFabric());
	}

	@Test
	public void workingDirectory() {
		Assert.assertEquals("testdirectory", SystemProperty.getInstance().getWorkingDirectory());
	}

	@Test
	public void faframPatchDir() {
		Assert.assertEquals("testdir", SystemProperty.getInstance().getPatchDir());
	}
}
