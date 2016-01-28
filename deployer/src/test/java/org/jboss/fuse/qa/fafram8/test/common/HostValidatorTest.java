package org.jboss.fuse.qa.fafram8.test.common;

import org.jboss.fuse.qa.fafram8.exception.ValidatorException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.resource.Fafram;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Host validator tests.
 * Created by avano on 22.9.15.
 */
@Ignore
public class HostValidatorTest {
	private Fafram fafram;

	@Test(expected = ValidatorException.class)
	public void nonExistentHostNameValidationTest() {
		System.setProperty(FaframConstant.HOST, "1.2.3.4");
		fafram = new Fafram();
		fafram.setup();
	}

	@Test(expected = ValidatorException.class)
	public void emptyHostValidationTest() {
		System.setProperty(FaframConstant.HOST, "");
		fafram = new Fafram();
		fafram.setup();
	}

	@After
	public void after() {
		System.clearProperty(FaframConstant.HOST);
		if (fafram != null) {
			fafram.tearDown();
		}
	}
}
