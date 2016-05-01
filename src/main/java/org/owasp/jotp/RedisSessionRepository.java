package org.owasp.jotp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * SessionRepository that holds one-time passwords in a Redis server. 
 */
public class RedisSessionRepository implements SessionRepository {

	private Logger log;
	private Jedis jedis;
	private boolean authenticated;
	private String password;
	
	public RedisSessionRepository(String host, int port, String password) {
		this.jedis = new Jedis(host, port);
		if (password == null) {
			authenticated = true;
		} else {
			authenticated = false;
		}
		this.log = LoggerFactory.getLogger(RedisSessionRepository.class);
		this.password = password;
	}

	@Override
	public void createSession(String sessionId, String token, LocalDateTime expireTime) throws ServiceException {
		if (!authenticated) {
			authenticate();
		}
		jedis.setex(sessionId, (int) ChronoUnit.SECONDS.between(LocalDateTime.now(), expireTime), token);
	}

	@Override
	public boolean isValid(String sessionId, String token) throws ServiceException {
		if (!authenticated) {
			authenticate();
		}
		
		String entry = jedis.get(sessionId);
		if (entry == null) {
			log.debug("No entry found in redis: <{}, {}>", sessionId, token);
			return false;
		}
		if (entry.equals(token)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void close() {
		// nop
	}
	
	/**
	 * Authenticate to Redis server, if necessary.
	 */
	private synchronized void authenticate() {
		if (authenticated) {
			return;
		}
		
		// Don't authenticate if there wasn't a password in the configuration
		if (password == null) {
			log.debug("No password in configuration, verifying connection...");
			try {
				String resp = jedis.ping();
				if ("PONG".equals(resp)) {
					// 'PONG' response received from server, we can connect without authentication
					authenticated = true;
				}
			} catch (JedisConnectionException e) {
				// Error connecting to the server
				log.error("Error connecting to Redis.  Exception message was: " + e.getMessage());
			} catch (JedisDataException e) {
				// Server requires a password
				if (e.getMessage().contains("NOAUTH"))
					log.error("Redis server requires a password.");
				else
					log.error("Redis error: " + e.getMessage());
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
	}
}
