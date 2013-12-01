package com.upcrob.jotp;

/**
 * Describes an Exception that occurs when sending a message fails.
 */
public class SenderException extends Exception {
	
	private static final long serialVersionUID = 4261185604071217608L;

	public SenderException(String msg) {
		super(msg);
	}
	
	public SenderException(String msg, Exception e) {
		super(msg, e);
	}
}
