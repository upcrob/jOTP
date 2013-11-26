package com.upcrob.jotp;

import java.security.SecureRandom;

/**
 * Utility class for generating random one-time passwords.
 */
public class TokenGenerator {
	
	private static final char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	private SecureRandom rand;
	private static TokenGenerator gen = new TokenGenerator();
	
	private TokenGenerator() {
		rand = new SecureRandom();
	}
	
	/**
	 * Returns a random password string with a length between
	 * the given minimum and maximum length.  If token passwords
	 * should always have a constant length, minLength and maxLength
	 * should be set to the same value.
	 */
	public String getToken(int minLength, int maxLength) {
		if (maxLength < minLength)
			throw new IllegalArgumentException("Maximum token length must be greater than minimum.");
		
		StringBuilder sb = new StringBuilder();
		int len = minLength + rand.nextInt(maxLength - minLength + 1);
		for (int i = 0; i < len; i++) {
			int index = rand.nextInt(alphabet.length);
			sb.append(alphabet[index]);
		}
		return sb.toString();
	}
	
	public static TokenGenerator getInstance() {
		return gen;
	}
}
