package com.upcrob.jotp;

import java.util.Map;
import java.util.Set;

/**
 * Defines a common interface for extracting system
 * configuration information.
 */
public interface Configuration {
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
	 * Get address that FROM field should
	 * be set to when sending token emails.
	 */
	public String getSmtpFrom();
	
	/**
	 * Returns whether SMTP is using transport
	 * layer security.
	 */
	public boolean isSmtpTls();
	
	/**
	 * Get type of authentication used for
	 * SMTP.  Returns null if authentication
	 * is not required.
	 */
	public AuthType getSmtpAuthType();
	
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
	 * Whether the controller should wait to see if the token
	 * could be sent successfully or if this should occur in a
	 * separate thread.  A value of false indicates that this the
	 * controller shouldn't wait to find out whether the send was
	 * successful, and should assume that it was.
	 */
	public boolean isBlockingSmtp();
	
	/**
	 * Get the set of Clients specified in the configuration.
	 * A Client holds the configuration for a specific client
	 * application or set of client applications (token lifetime, etc).
	 */
	public Map<String, Client> getClients();
	
	/**
	 * Get the type of Tokenstore to use.  Valid values are as follows:
	 *   local - Tokens stored in-memory.  Best for single instance
	 *     installations.
	 *   jdbc - Tokens stored in an external relational database.
	 *   redis - Tokens stored in an external Redis server.
	 */
	public TokenstoreType getTokenstoreType();
	
	/**
	 * Get the JDBC connection string used when the Tokenstore type
	 * is set to 'jdbc'.
	 */
	public String getJdbcString();
	
	/**
	 * Get the hostname of the Redis server, if Redis is being used
	 * as the tokenstore.
	 */
	public String getRedisHost();
	
	/**
	 * Get the port that the Redis server is listening on.  If no
	 * port has been defined in the Configuration, this method will
	 * return -1.
	 */
	public int getRedisPort();
	
	/**
	 * Get the password required to authenticate to a Redis server.
	 * Returns null if no password is set and/or required.
	 */
	public String getRedisPassword();
}
