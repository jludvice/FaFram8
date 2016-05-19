package org.jboss.fuse.qa.fafram8.openstack.exception;

/**
 * Invoker pool interrupted exception class.
 * Created by ecervena on 9.10.15.
 */
public class InvokerPoolInterruptedException extends RuntimeException {
	/**
	 * Constructor.
	 */
	public InvokerPoolInterruptedException() {
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 */
	public InvokerPoolInterruptedException(String message) {
		super(message);
	}
}
