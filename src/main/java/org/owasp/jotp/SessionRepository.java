package org.owasp.jotp;

import java.time.LocalDateTime;

/**
 * Describes a repository for storing one-time passwords.
 */
public interface SessionRepository {
	
	/**
	 * Creates a session and associated OTP token.
	 * @param sessionId Session ID.
	 * @param token OTP token.
	 * @param expireTime The time at which the OTP token becomes invalid.
	 * @throws ServiceException thrown if something goes wrong in the underlying repository implementation.
	 */
	void createSession(String sessionId, String token, LocalDateTime expireTime) throws ServiceException;
	
	/**
	 * Determine if a OTP token is valid.
	 * @param sessionId Session ID.
	 * @param token OTP token.
	 * @return If session is valid.
	 * @throws ServiceException thrown if something goes wrong in the underlying repository implementation.
	 */
	boolean isValid(String sessionId, String token) throws ServiceException;
	
	/**
	 * Takes care of any repository cleanup.
	 */
	void close();
}
