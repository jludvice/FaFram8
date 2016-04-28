package org.jboss.fuse.qa.fafram8.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jira annotation.
 * Created by avano on 29.10.15.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Jira {
	/**
	 * Default annotation method.
	 */
	String[] value();
}
