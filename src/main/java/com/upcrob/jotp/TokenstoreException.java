package com.upcrob.jotp;

/**
 * Describes an Exception thrown when an error occurs when interacting with a
 * Tokenstore.
 */
public class TokenstoreException extends Exception {
	
	private static final long serialVersionUID = -9055352157008071489L;

	public TokenstoreException(String msg) {
		super(msg);
	}
	
	public TokenstoreException(String msg, Exception e) {
		super(msg, e);
	}
}
