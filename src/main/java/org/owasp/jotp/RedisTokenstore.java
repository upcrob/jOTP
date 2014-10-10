package org.owasp.jotp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * Tokenstore that holds one-time passwords in a Redis server. 
 */
public class RedisTokenstore implements Tokenstore {

	private Logger log;
	private Jedis jedis;
	private Map<String, Client> clients;
	private boolean authenticated;
	private String password;
	
	public RedisTokenstore(Configuration config) {
		log = LoggerFactory.getLogger(RedisTokenstore.class);
		clients = config.getClients();
		
		// Setup Redis client
		if (config.getRedisPort() == -1)
			jedis = new Jedis(config.getRedisHost());	// No port set in configuration
		else
			jedis = new Jedis(config.getRedisHost(), config.getRedisPort());
		
		// Store Redis password for authentication
		password = config.getRedisPassword();
		authenticate();
	}
	
	@Override
	public void putToken(String client, String uid, String token) throws TokenstoreException {
		log.debug("Attempting to add token, '"
				+ token
				+ "' for client '"
				+ client
				+ "' under UID, '"
				+ uid
				+ "'");
		
		// Authenticate if we aren't already
		if (!authenticated)
			if (!authenticate())
				throw new TokenstoreException("Authentication failed.");
		
		// Compute token expiration time
		Client c = clients.get(client);
		
		// Setup and execute transaction
		try {
			jedis.setex(client + ":" + uid, c.getTokenLifetime(), token);
			log.debug("Token, '" + token + "' added.");
		} catch (JedisConnectionException e) {
			log.error("Error connecting to Redis.  Exception message was: "
					+ e.getMessage());
			throw new TokenstoreException("Error connecting to Redis server.", e);
		}
	}

	@Override
	public boolean isTokenValid(String client, String uid, String token) throws TokenstoreException {
		log.debug("Checking token validity: (" + client + "," + uid + "," + token + ")");
		
		// Authenticate if we aren't already
		if (!authenticated)
			if (!authenticate())
				throw new TokenstoreException("Authentication failed.");
		
		try {
			String tokenString = jedis.get(client + ":" + uid);
			if (tokenString == null) {
				log.debug("Token, '{}' does not exist in tokenstore.", token);
				return false;
			} else if (!tokenString.equals(token)) {
				log.debug("Found match for key, but token values did not match.");
				return false;
			} else {
				log.debug("Token, '{}' was valid.", token);
				jedis.del(client + ":" + uid);
				return true;
			}
		} catch (JedisConnectionException e) {
			log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
			throw new TokenstoreException("Error connecting to Redis server.", e);
		}
	}

	@Override
	public void removeExpired() throws TokenstoreException {
		throw new UnsupportedOperationException("Redis server handles token expiration.");
	}

	/**
	 * Authenticate to Redis server, if necessary.
	 */
	private boolean authenticate() {
		// Don't authenticate if there wasn't a password in the configuration
		if (password == null) {
			log.debug("No password in configuration, verifying connection...");
			try {
				String resp = jedis.ping();
				if ("PONG".equals(resp)) {
					// 'PONG' response received from server, we can connect without authentication
					authenticated = true;
					return true;
				} else {
					// Unknown response received
					return false;
				}
			} catch (JedisConnectionException e) {
				// Error connecting to the server
				log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
				return false;
			} catch (JedisDataException e) {
				// Server requires a password
				if (e.getMessage().contains("NOAUTH"))
					log.error("Redis server requires a password.");
				else
					log.error("Redis error: " + e.getMessage());
				return false;
			}
		}
		
		// Try to authenticate to the Redis server
		log.debug("Attempting to authenticate to Redis server...");
		try {
			String resp = jedis.auth(password);
			if ("OK".equals(resp)) {
				// Authentication succeeded
				log.debug("Authentication successful.");
				authenticated = true;
				return true;
			}
		} catch (JedisConnectionException e) {
			// Error connecting to server
			log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
		} catch (JedisDataException e) {
			// Authentication failed
			if (e.getMessage().contains("NOAUTH"))
				log.error("Error authenticating to Redis server.");
			else
				log.error("Redis error: " + e.getMessage());
		}
		return false;
	}

	@Override
	public boolean requiresReaper() {
		return false;
	}
}
