package org.jboss.fuse.qa.fafram8.exception;

/**
 * Bundle uploader exception.
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class BundleUploadException extends RuntimeException {

	public BundleUploadException() {
		super();
	}

	public BundleUploadException(Throwable cause) {
		super(cause);
	}

	public BundleUploadException(String message) {
		super(message);
	}

	public BundleUploadException(String message, Throwable cause) {
		super(message, cause);
	}

	protected BundleUploadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
