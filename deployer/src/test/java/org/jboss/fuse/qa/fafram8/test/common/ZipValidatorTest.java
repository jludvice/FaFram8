package org.jboss.fuse.qa.fafram8.test.common;

import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Validator test class.
 * Created by avano on 21.9.15.
 */
@Ignore
public class ZipValidatorTest {
	private Fafram fafram;

	@Test(expected = ValidatorException.class)
	public void fuseZipValidationTest() {
		System.setProperty(FaframConstant.FUSE_ZIP, "");
		fafram = new Fafram();
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void fuseHostZipValidationTest() {
		System.setProperty(FaframConstant.HOST, "1.2.3.4");
		fafram = new Fafram();
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void fuseNonExistentZipValidationTest() {
		System.setProperty(FaframConstant.FUSE_ZIP, "file:///nonexistent");
		fafram = new Fafram();
		fafram.setup();
	}

	@After
	public void after() {
		System.clearProperty(FaframConstant.FUSE_ZIP);
		System.clearProperty(FaframConstant.HOST);
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
