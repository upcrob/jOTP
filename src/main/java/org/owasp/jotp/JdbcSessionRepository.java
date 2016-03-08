package org.owasp.jotp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SessionRepository that stores tokens in a relational database over JDBC.
 * 
 * Table schema:
 *   session    VARCHAR(255) PRIMARY KEY
 *   token      VARCHAR(255) PRIMARY KEY
 *   expiretime TIMESTAMP
 */
public class JdbcSessionRepository implements SessionRepository {
	
	private String connectionString;
	
	private final String PUT_STATEMENT;
	private final String DELETE_STATEMENT;
	private final String BATCH_EXPIRE_STATEMENT;
	
	private final ScheduledThreadPoolExecutor cleanupScheduler;
	private final ArrayBlockingQueue<Connection> pool;
	private static final int POOL_SIZE = 20;
	
	private final Logger logger;
	
	public JdbcSessionRepository(String connectionString, String tableName) {
		this.connectionString = connectionString;
		
		logger = LoggerFactory.getLogger(JdbcSessionRepository.class);
		
		PUT_STATEMENT = "INSERT INTO " + tableName + " (session, token, expiretime)"
				+ " VALUES (?, ?, ?)";
		DELETE_STATEMENT = "DELETE FROM " + tableName
				+ " WHERE session = ? AND token = ? AND expiretime > ?"; 
		BATCH_EXPIRE_STATEMENT = "DELETE FROM " + tableName
				+ " WHERE expiretime <= ?";
		
		pool = new ArrayBlockingQueue<>(POOL_SIZE);
		try {
			initConnectionPool();
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to setup connection to database.", e);
		}
		
		cleanupScheduler = new ScheduledThreadPoolExecutor(1);
		cleanupScheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("Started cleanup.");
					PreparedStatement stmt = getConnection().prepareStatement(BATCH_EXPIRE_STATEMENT);
					stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
					stmt.executeUpdate();
					logger.info("Finished cleanup.");
				} catch (SQLException e) {
					logger.error("Error during cleanup operation.", e);
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
		
	}
	
	@Override
	public void createSession(String sessionId, String token, LocalDateTime expireTime) throws ServiceException {
		try {
			PreparedStatement stmt = getConnection().prepareStatement(PUT_STATEMENT);
			stmt.setString(1, sessionId);
			stmt.setString(2, token);
			stmt.setTimestamp(3, new Timestamp(toMillis(expireTime)));
			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated != 1) {
				throw new ServiceException("Failed to save token to database.");
			}
			stmt.close();
		} catch (SQLException e) {
			throw new ServiceException("Database error.", e);
		}
	}

	@Override
	public boolean isValid(String sessionId, String token) throws ServiceException {
		try {
			PreparedStatement stmt = getConnection().prepareStatement(DELETE_STATEMENT);
			stmt.setString(1, sessionId);
			stmt.setString(2, token);
			stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			int rowsDeleted = stmt.executeUpdate();
			return rowsDeleted > 0;
		} catch (SQLException e) {
			throw new ServiceException("Database error.", e);
		}
	}
	
	@Override
	public void close() {
		cleanupScheduler.shutdown();
		for (int i = 0; i < POOL_SIZE; i++) {
			try {
				pool.take().close();
			} catch (SQLException e) {
				logger.warn("Failed to close connection.", e);
			} catch (InterruptedException e) {
				logger.warn("Failed to close connection.", e);
			}
		}
	}
	
	private long toMillis(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
	
	private void initConnectionPool() throws SQLException {
		for (int i = 0; i < POOL_SIZE; i++) {
			pool.add(DriverManager.getConnection(connectionString));
		}
	}
	
	private Connection getConnection() throws SQLException {
		try {
			return pool.take();
		} catch (InterruptedException e) {
			throw new SQLException("Failed to get connection.", e);
		}
	}
}