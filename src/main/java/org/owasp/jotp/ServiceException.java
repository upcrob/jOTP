package org.owasp.jotp;

/**
 * Exception thrown when an error occurs in an underlying service.
 */
public class ServiceException extends Exception {
	private static final long serialVersionUID = 83226969398952180L;

	public ServiceException(String msg) {
		super(msg);
	}
	
	public ServiceException(String msg, Exception e) {
		super(msg, e);
	}
}
