package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Local patch tests.
 * Created by avano on 2.9.15.
 */
@Slf4j
@Ignore
public class LocalPatchTest {
	private Fafram fafram;

	@Test
	public void karafLatestPatchTest() {
		System.setProperty(FaframConstant.PATCH, "latest");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram();
		fafram.setup();
		assertTrue(fafram.executeCommand("patch:list | grep jboss-fuse").contains("true"));
	}

	@Test
	public void fabricLatestPatchTest() {
		System.setProperty(FaframConstant.PATCH, "latest");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram().withFabric();
		fafram.setup();
		String version = fafram.executeCommand("version-list | tail -n 1").split(" ")[0];
		assertTrue(fafram.executeCommand("container-list | grep root").contains(version));
	}

	@Test
	public void karafFilePatchTest() {
		System.setProperty(FaframConstant.PATCH, "file:///home/fuse/patches/latest/jboss-fuse-full-6.2.1.redhat-083.zip");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram();
		fafram.setup();
		assertTrue(fafram.executeCommand("patch:list | grep jboss-fuse").contains("true"));
	}

	@Test
	public void fabricFilePatchTest() {
		System.setProperty(FaframConstant.PATCH, "file:///home/fuse/patches/latest/jboss-fuse-full-6.2.1.redhat-083.zip");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram().withFabric();
		fafram.setup();
		String version = fafram.executeCommand("version-list | tail -n 1").split(" ")[0];
		assertTrue(fafram.executeCommand("container-list | grep root").contains(version));
	}

	@Test
	public void karafHttpPatchTest() {
		System.setProperty(FaframConstant.PATCH, FaframTestBase.getVersion("6.2.1.redhat-082"));
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");

		fafram = new Fafram();
		fafram.setup();
		assertTrue(fafram.executeCommand("patch:list | grep jboss-fuse").contains("true"));
	}

	@Test
	public void fabricHttpPatchTest() {
		System.setProperty(FaframConstant.PATCH, FaframTestBase.getVersion("6.2.1.redhat-082"));
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram().withFabric();
		fafram.setup();
		String version = fafram.executeCommand("version-list | tail -n 1").split(" ")[0];
		assertTrue(fafram.executeCommand("container-list | grep root").contains(version));
	}

	@Test
	public void karafScpPatchTest() {

	}

	@Test
	public void fabricScpPatchTest() {

	}

	@Test
	public void karafStringPatchTest() {
		System.setProperty(FaframConstant.PATCH, "081");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");

		fafram = new Fafram();
		fafram.setup();
		assertTrue(fafram.executeCommand("patch:list | grep jboss-fuse").contains("true"));
	}

	@Test
	public void fabricStringPatchTest() {
		System.setProperty(FaframConstant.PATCH, "081");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram().withFabric();
		fafram.setup();
		String version = fafram.executeCommand("version-list | tail -n 1").split(" ")[0];
		assertTrue(fafram.executeCommand("container-list | grep root").contains(version));
	}

	@Test
	public void karafMultiplePatchesTest() {
		System.setProperty(FaframConstant.PATCH, "081,083");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");

		fafram = new Fafram();
		fafram.setup();
		assertEquals("2", fafram.executeCommand("patch:list | grep -c true"));
	}

	@Test
	public void fabricMultiplePatchesTest() {
		System.setProperty(FaframConstant.PATCH, "081,083");
		log.info("System property is \'" + System.getProperty(FaframConstant.PATCH) + "\'");
		fafram = new Fafram().withFabric();
		fafram.setup();
		String version = fafram.executeCommand("version-list | tail -n 1").split(" ")[0];
		assertTrue(fafram.executeCommand("container-list | grep root").contains(version));
	}

	@After
	public void after() {
		if (fafram != null) {
			fafram.tearDown();
		}
		System.clearProperty(FaframConstant.PATCH);
	}
}
