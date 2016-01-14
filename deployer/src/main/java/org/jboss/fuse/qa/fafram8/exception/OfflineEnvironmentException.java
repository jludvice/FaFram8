package org.jboss.fuse.qa.fafram8.exception;

/**
 * Class representing exception with setting offline environment.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class OfflineEnvironmentException extends RuntimeException {

	/**
	 * Constructor.
	 */
	public OfflineEnvironmentException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param cause cause
	 */
	public OfflineEnvironmentException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public OfflineEnvironmentException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 * @param cause cause
	 */
	public OfflineEnvironmentException(String message, Throwable cause) {
		super(message, cause);
	}
}
