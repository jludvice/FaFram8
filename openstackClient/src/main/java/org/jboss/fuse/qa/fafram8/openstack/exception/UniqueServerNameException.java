package org.jboss.fuse.qa.fafram8.openstack.exception;

/**
 * Unique server name exception class.
 * Created by ecervena on 24.9.15.
 */
public class UniqueServerNameException extends RuntimeException {
	/**
	 * Constructor.
	 */
	public UniqueServerNameException() {
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public UniqueServerNameException(String message) {
		super(message);
	}
}
