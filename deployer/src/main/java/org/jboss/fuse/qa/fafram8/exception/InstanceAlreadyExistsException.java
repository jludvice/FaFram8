package org.jboss.fuse.qa.fafram8.exception;

/**
 * Instance already exists exception. Used in openstack provision provider when the instance with given name already exists.
 * Created by avano on 8.2.16.
 */
public class InstanceAlreadyExistsException extends RuntimeException {

	/**
	 * Constructor.
	 */
	public InstanceAlreadyExistsException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public InstanceAlreadyExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param cause cause
	 */
	public InstanceAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 * @param cause cause
	 */
	public InstanceAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
