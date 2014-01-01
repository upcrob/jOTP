package com.upcrob.jotp;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

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
		long expires = System.currentTimeMillis() + (c.getTokenLifetime() * 1000);
		
		// Setup and execute transaction
		try {
			Transaction t = jedis.multi();
			String key = client + uid + token;
			t.hset(key, "expiration", String.valueOf(expires));
			t.exec();
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
			String key = client + uid + token;
			String expireString = jedis.hget(key, "expiration");
			if (expireString == null) {
				log.debug("Token, '" + token + "' does not exist in tokenstore.");
				return false;	// Token does not exist in Redis
			}
			
			long expires = Long.parseLong(expireString);
			if (expires > System.currentTimeMillis()) {
				// Token was valid, remove it from Redis
				log.debug("Token, '" + token + "' was valid.");
				jedis.del(key);
				return true;
			} else {
				// Token was not valid, remove it from Redis
				log.debug("Token, '" + token + "' was in tokenstore but has expired.");
				jedis.del(key);
				return false;
			}
		} catch (JedisConnectionException e) {
			log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
			throw new TokenstoreException("Error connecting to Redis server.", e);
		}
	}

	@Override
	public void removeExpired() throws TokenstoreException {
		log.debug("Removing expired tokens from Redis...");
		
		// Authenticate if we aren't already
		if (!authenticated)
			if (!authenticate())
				throw new TokenstoreException("Authentication failed.");
		
		try {
			Set<String> keys = jedis.keys("*");
			for (String key : keys) {
				String expString = jedis.hget(key, "expiration");
				if (expString == null)
					continue;
				
				long expires = Long.parseLong(expString);
				if (expires < System.currentTimeMillis()) {
					// Token has expired, remove it from Redis
					log.debug("Removing expired key from Redis: " + key);
					jedis.del(key);
				}
			}
		} catch (JedisConnectionException e) {
			log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
			throw new TokenstoreException("Error connecting to Redis server.", e);
		}
		log.debug("Done removing expired tokens from Redis.");
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
					authenticated = true;
					return true;
				} else {
					return false;
				}
			} catch (JedisConnectionException e) {
				log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
				return false;
			} catch (JedisDataException e) {
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
				log.debug("Authentication successful.");
				authenticated = true;
				return true;
			}
		} catch (JedisConnectionException e) {
			log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
		} catch (JedisDataException e) {
			if (e.getMessage().contains("NOAUTH"))
				log.error("Error authenticating to Redis server.");
			else
				log.error("Redis error: " + e.getMessage());
		}
		return false;
	}
}
