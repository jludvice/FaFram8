package org.jboss.fuse.qa.fafram8.exception;

/**
 * Bundle uploader exception.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class BundleUploadException extends RuntimeException {

	/**
	 * Constructor.
	 */
	public BundleUploadException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param cause cause
	 */
	public BundleUploadException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message message
	 * @param cause cause
	 */
	public BundleUploadException(String message, Throwable cause) {
		super(message, cause);
	}
}
