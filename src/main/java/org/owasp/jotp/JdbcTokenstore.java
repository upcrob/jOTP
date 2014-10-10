package org.owasp.jotp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tokenstore that holds one-time use passwords in
 * a JDBC datasource.  This class assumes that
 * the JDBC driver has already been loaded
 * onto the classpath and is available using the
 * DriverManager.
 */
public class JdbcTokenstore implements Tokenstore {

	private Configuration config;
	private String connString;
	private Logger log;
	
	private static final String PUT_STATEMENT = "INSERT INTO tokenstore (client, uid, token, expiration)"
			+ " VALUES (?, ?, ?, ?)";
	private static final String GET_STATEMENT = "DELETE FROM tokenstore WHERE ("
			+ "client = ? AND uid = ? AND token = ? AND expiration >= ?)";
	private static final String EXPIRE_STATEMENT = "DELETE FROM tokenstore WHERE ("
			+ "expiration < ?)";
	
	public JdbcTokenstore(Configuration config) {
		this.log = LoggerFactory.getLogger(JdbcTokenstore.class);
		this.config = config;
		this.connString = config.getJdbcString();
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
		
		
		// Compute expiration timestamp
		Client c = config.getClients().get(client);
		long expiration = (c.getTokenLifetime() * 1000) + System.currentTimeMillis();
		
		// Try to connect to database
		Connection conn = null;
		try {
			// Update database
			conn = DriverManager.getConnection(connString);
			PreparedStatement stmt = conn.prepareStatement(PUT_STATEMENT);
			stmt.setString(1, client);
			stmt.setString(2, uid);
			stmt.setString(3, token);
			stmt.setLong(4, expiration);
			int numChanges = stmt.executeUpdate();
			conn.commit();
			stmt.close();
			
			// Log changes
			if (numChanges > 0)
				log.debug("Token added successfully: " + token);
			else
				log.warn("No rows updated when trying to add token for user: " + uid);
		} catch (SQLException e) {
			log.error("An error occurred while accessing the JDBC datasource: "
					+ e.getMessage());
			throw new TokenstoreException("Error occurred while accessing JDBC datasource.", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.warn("JDBC connection wasn't closed properly.  This may cause a resource leak.  "
						+ "Exception message was: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public boolean isTokenValid(String client, String uid, String token) throws TokenstoreException {
		log.debug("Checking token validity: (" + client + "," + uid + "," + token + ")");
		
		boolean tokenValid = false;
		
		// Try to connect to database
		Connection conn = null;
		try {
			// Update database
			conn = DriverManager.getConnection(connString);
			PreparedStatement stmt = conn.prepareStatement(GET_STATEMENT);
			stmt.setString(1, client);
			stmt.setString(2, uid);
			stmt.setString(3, token);
			stmt.setLong(4, System.currentTimeMillis());
			int updates = stmt.executeUpdate();
			conn.commit();
			stmt.close();
			if (updates > 0)
				tokenValid = true;
		} catch (SQLException e) {
			log.error("An error occurred while accessing the JDBC datasource: "
					+ e.getMessage());
			throw new TokenstoreException("Error occurred while accessing the JDBC datasource.", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.warn("JDBC connection wasn't closed properly.  This may cause a resource leak.  "
						+ "Exception message was: " + e.getMessage());
				}
			}
		}
		
		log.debug("Token '" + token + "' valid: " + tokenValid);
		return tokenValid;
	}

	@Override
	public void removeExpired() throws TokenstoreException {
		log.debug("Removing expired tokens from JDBC tokenstore...");
		
		// Try to connect to database
		Connection conn = null;
		try {
			// Update database
			conn = DriverManager.getConnection(connString);
			PreparedStatement stmt = conn.prepareStatement(EXPIRE_STATEMENT);
			stmt.setLong(1, System.currentTimeMillis());
			int updates = stmt.executeUpdate();
			conn.commit();
			stmt.close();
			log.debug("Removed " + updates + " rows from the tokenstore.");
		} catch (SQLException e) {
			log.error("An error occurred while accessing the JDBC datasource: "
					+ e.getMessage());
			throw new TokenstoreException("Error occurred while accessing the JDBC datasource.", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.warn("JDBC connection wasn't closed properly.  This may cause a resource leak.  "
						+ "Exception message was: " + e.getMessage());
				}
			}
		}
		log.debug("Done removing expired tokens.");
	}

	@Override
	public boolean requiresReaper() {
		return true;
	}
}
