package org.owasp.jotp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tokenstore that holds one-time password tokens
 * in local memory.  This Tokenstore should not be
 * used if a multi-instance (clustered) failover
 * environment is needed.
 */
public class LocalTokenstore implements Tokenstore {
	private Map<String, Map<String, Token>> clientMap;
	private Map<String, Client> clients;
	private Logger log;
	
	public LocalTokenstore(Configuration config) {
		// Get map of clients for reading
		clients = config.getClients();
		
		// Add map for holding each client's tokens
		clientMap = new HashMap<String, Map<String, Token>>();
		Set<String> clientSet = clients.keySet();
		for (String clientName : clientSet) {
			clientMap.put(clientName, new HashMap<String, Token>());
		}
		log = LoggerFactory.getLogger(LocalTokenstore.class);
	}
	
	@Override
	public void removeExpired() {
		synchronized (clientMap) {
			log.debug("Removing expired tokens from local tokenstore...");
			long time = System.currentTimeMillis();
			Set<Entry<String, Map<String, Token>>> clients = clientMap.entrySet();
			for (Entry<String, Map<String, Token>> client : clients) {
				// Get token map for this client
				Map<String, Token> otpMap = client.getValue();
				
				Set<Entry<String, Token>> tokens = otpMap.entrySet();
				for (Entry<String, Token> entry : tokens) {
					Token token = entry.getValue();
					if (token.getExpireTime() <= time) {
						String key = entry.getKey();
						log.debug("Removed: <"
								+ key
								+ ", "
								+ token.getValue()
								+ "> from client pool: "
								+ client.getKey());
						otpMap.remove(key);
					}
				}
			}
			log.debug("Done removing expired tokens.");
		}
	}

	@Override
	public void putToken(String client, String uid, String token) {
		Map<String, Token> otpMap = clientMap.get(client);
		if (otpMap != null) {
			// Get client timeout configuration
			Client c = clients.get(client);
			
			// Add token to map
			synchronized (otpMap) {
				otpMap.put(uid, new Token(token, c.getTokenLifetime()));
			}
			log.debug("Added " + token + " for client, '"
					+ client + "' under uid, '" + uid + "'");
		}
	}

	@Override
	public boolean isTokenValid(String client, String uid, String token) {
		if (client == null)
			throw new IllegalArgumentException("Client must not be null.");
		if (uid == null)
			throw new IllegalArgumentException("UID must not be null.");
		if (token == null)
			throw new IllegalArgumentException("Token value must not be null.");
		
		// Get the token map for this client
		Map<String, Token> otpMap = clientMap.get(client);
		if (otpMap == null) {
			log.debug("Client, '"
					+ client
					+ "' was requested  but does not exist in lookup table");
			return false;
		}
			
		synchronized (otpMap) {
			// Make sure key exists in the map
			Token mapValue = otpMap.get(uid);
			if (mapValue == null) {
				log.debug("Key, '"
						+ uid
						+ "' was requested but does not exist in lookup table.");
				return false;
			}
			
			// Make sure the input token matches the map token
			String tokenString = mapValue.getValue();
			if (!tokenString.equals(token)) {
				log.debug("Key, '"
						+ uid
						+ "' was found in lookup table, but password token did not match.");
				return false;
			}
			
			// Make sure the token hasn't expired
			if (System.currentTimeMillis() > mapValue.getExpireTime()) {
				otpMap.remove(uid);	// The token has expired, remove it
				log.debug("Key, '"
						+ uid
						+ "' was found in lookup table, but has expired.");
				return false;
			}
			
			// If we make it here, the token is valid
			// Remove the token and return
			otpMap.remove(uid);
			return true;
		}
	}
	
	@Override
	public boolean requiresReaper() {
		return true;
	}
	
	/**
	 * Describes a one-time password token that has been
	 * generated and stored in memory.
	 */
	private static class Token {
		
		private String value;
		private long expireTime;
		
		/**
		 * Creates a new Token with a given one-time password
		 * value and lifetime (in seconds).
		 */
		public Token(String value, int lifetime) {
			this.value = value;
			this.expireTime = System.currentTimeMillis() + (lifetime * 1000);
		}

		/**
		 * Returns the one-time password token's value.
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Returns the time at which this token is set to
		 * expire (milliseconds since Jan. 1 1970).
		 */
		public long getExpireTime() {
			return expireTime;
		}
	}
}
