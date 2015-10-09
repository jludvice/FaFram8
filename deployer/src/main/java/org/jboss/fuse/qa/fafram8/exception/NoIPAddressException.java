package org.jboss.fuse.qa.fafram8.exception;

/**
 * Created by ecervena on 9.10.15.
 */
public class NoIPAddressException extends RuntimeException {
	/**
	 * Constructor.
	 */
	public NoIPAddressException() {
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public NoIPAddressException(String message) {
		super(message);
	}
}
