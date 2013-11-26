package com.upcrob.jotp;

/**
 * Describes a one-time password token that has been
 * generated and stored in memory.
 */
public class Token {
	
	private String value;
	private int lifetime;
	private long expireTime;
	
	/**
	 * Creates a new Token with a given one-time password
	 * value and lifetime (in seconds).
	 */
	public Token(String value, int lifetime) {
		this.value = value;
		this.lifetime = lifetime;
		this.expireTime = System.currentTimeMillis() + (lifetime * 1000);
	}

	/**
	 * Returns the one-time password token's value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the maximum token lifetime in seconds.
	 */
	public int getLifetime() {
		return lifetime;
	}

	/**
	 * Returns the time at which this token is set to
	 * expire (milliseconds after Jan. 1 1970).
	 */
	public long getExpireTime() {
		return expireTime;
	}
}
