package org.jboss.fuse.qa.fafram8.test.common;

import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Patch validator tests.
 * Created by avano on 22.9.15.
 */
@Ignore
public class PatchValidatorTest {
	private Fafram fafram;

	@Test(expected = ValidatorException.class)
	public void nonExistentPatchNameValidationTest() {
		System.setProperty(FaframConstant.PATCH, "nonexistent");
		fafram = new Fafram();
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void fuseEmptyHostValidationTest() {
		System.setProperty(FaframConstant.PATCH, "file:///nonexistent");
		fafram = new Fafram();
		fafram.setup();
	}

	@After
	public void after() {
		System.clearProperty(FaframConstant.PATCH);
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
