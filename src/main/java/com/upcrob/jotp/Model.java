package com.upcrob.jotp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton model object that holds the state of
 * one-time passwords currently in use as well as
 * other relevant runtime data.
 */
public class Model {
	// Reference to the singleton
	private static Model model = new Model();
	
	// Internal map of one-time passwords, indexed by
	// their associated IDs (eg. user ID)
	private Map<String, Token> otpMap;
	
	private Logger log;
	
	private Model() {
		otpMap = new HashMap<String, Token>();
		log = LoggerFactory.getLogger(Model.class);
	}
	
	public void putToken(String key, String tokenString, int lifetime) {
		otpMap.put(key, new Token(tokenString, lifetime));
	}
	
	public boolean isTokenValid(String key, String token) {
		if (key == null)
			throw new IllegalArgumentException("Key must not be null.");
		if (token == null)
			throw new IllegalArgumentException("Token value must not be null.");
		
		// Make sure key exists in the map
		Token mapValue = otpMap.get(key);
		if (mapValue == null) {
			log.debug("Key, '" + key + "' was requested but does not exist in lookup table.");
			return false;
		}
		
		// Make sure the input token matches the map token
		String tokenString = mapValue.getValue();
		if (!tokenString.equals(token)) {
			log.debug("Key, '" + key + "' was found in lookup table, but password token did not match.");
			return false;
		}
		
		// Make sure the token hasn't expired
		if (System.currentTimeMillis() > mapValue.getExpireTime()) {
			otpMap.remove(key);	// The token has expired, remove it
			log.debug("Key, '" + key + "' was found in lookup table, but has expired.");
			return false;
		}
		
		// If we make it here, the token is valid
		return true;
	}
	
	/**
	 * Removes all expired tokens stored in the model.
	 */
	public void removeExpired() {
		synchronized (otpMap) {
			log.debug("Removing expired tokens from model...");
			Set<Entry<String, Token>> entries = otpMap.entrySet();
			for (Entry<String, Token> entry : entries) {
				Token token = entry.getValue();
				long time = System.currentTimeMillis();
				if (token.getExpireTime() <= time) {
					String key = entry.getKey();
					log.debug("Removed: " + key + " | " + token.getValue() + " from model.");
					otpMap.remove(key);
				}
			}
			log.debug("Done removing expired tokens.");
		}
	}
	
	/**
	 * Removes a token corresponding to the given key (user ID).
	 */
	public void removeToken(String key) {
		otpMap.remove(key);
	}
	
	/**
	 * Returns the singleton instance of the Model.
	 */
	public static Model getInstance() {
		return model;
	}
}
