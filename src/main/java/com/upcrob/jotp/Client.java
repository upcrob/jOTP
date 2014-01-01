package com.upcrob.jotp;

/**
 * Describes the configuration associated with a Client.  A Client has an
 * isolated pool of generated OTP tokens.  This allows multiple applications
 * to use a single instance of jOTP with different configurations (token
 * lifetimes, etc).
 * 
 * The default configuration for a Client is as follows:
 *   password: none
 *   minOtpLength: 8 characters
 *   maxOtpLength: 8 characters
 *   tokenLifetime: 60 seconds
 */
public class Client {
	private String password;
	private int minOtpLength;
	private int maxOtpLength;
	private int tokenLifetime;
	
	public Client() {
		password = null;
		minOtpLength = 8;
		maxOtpLength = 8;
		tokenLifetime = 60;
	}
	
	/**
	 * Get the password for the client.  If there is no password
	 * for this client, it is considered public.  That is, any
	 * client application can generate an OTP token.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns the minimum OTP String length.
	 * This must not be greater than the maximum length.
	 */
	public int getMinOtpLength() {
		return minOtpLength;
	}
	
	/**
	 * Returns the maximum OTP String length.  This
	 * must not be less than the minimum length.
	 */
	public int getMaxOtpLength() {
		return maxOtpLength;
	}
	
	/**
	 * Get the maximum lifetime of a one-time
	 * password in seconds.
	 */
	public int getTokenLifetime() {
		return tokenLifetime;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMinOtpLength(int minOtpLength) {
		this.minOtpLength = minOtpLength;
	}

	public void setMaxOtpLength(int maxOtpLength) {
		this.maxOtpLength = maxOtpLength;
	}

	public void setTokenLifetime(int tokenLifetime) {
		this.tokenLifetime = tokenLifetime;
	}
}
