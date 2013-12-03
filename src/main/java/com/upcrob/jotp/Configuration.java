package com.upcrob.jotp;

import java.util.Set;

/**
 * Defines a common interface for extracting system
 * configuration information.
 */
public interface Configuration {
	/**
	 * Minimum OTP length.  This should not
	 * be greater than the maximum length.
	 */
	public int getOtpMinLength();
	
	/**
	 * Maximum one-time password length.  This
	 * should not be greater than the minimum length.
	 */
	public int getOtpMaxLength();
	
	/**
	 * Get the hostname of the SMTP server.
	 */
	public String getSmtpHost();
	
	/**
	 * Get the port at the host that the SMTP
	 * server is listening on.
	 */
	public int getSmtpPort();
	
	/**
	 * Get the username of the email account
	 * being used to send OTP tokens.
	 */
	public String getSmtpUsername();
	
	/**
	 * Get the password of the email account
	 * being used to send OTP tokens.
	 */
	public String getSmtpPassword();
	
	/**
	 * Get the set of hosts that text OTP tokens
	 * will be sent to when requested.  To send
	 * a text to a phone, an email will be
	 * sent to <PHONE NUMBER>@<HOST>
	 * 
	 * For example, for texts to be sent to
	 * Verizon phones, the host, 'vtext.com'
	 * should be added to the set of provider
	 * hosts.
	 */
	public Set<String> getMobileProviderHosts();
	
	/**
	 * Get the maximum lifetime of a one-time
	 * password.
	 */
	public int getTokenLifetime();
	
	/**
	 * Whether or not responses from the token controllers
	 * should be optimistic.  That is, whether the controller
	 * should wait to see if the token could be sent successfully
	 * or if this should occur in a separate thread.  A value
	 * of true indicates that this the controller shouldn't
	 * wait to find out whether the send was successful, and
	 * should assume that it was.
	 */
	public boolean isOptimisticResponse();
}
