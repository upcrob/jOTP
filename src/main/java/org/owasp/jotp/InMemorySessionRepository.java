package org.owasp.jotp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionRepository implementation that stores OTP tokens in local memory.  Note
 * that this is not a distributed implementation and should not be used in
 * production environments.
 */
public class InMemorySessionRepository implements SessionRepository {

	private Map<String, Session> map;
	
	public InMemorySessionRepository() {
		map = new ConcurrentHashMap<>();
	}
	
	@Override
	public void createSession(String sessionId, String token,
			LocalDateTime expireTime) {
		map.put(sessionId, new Session(token, expireTime));
	}

	@Override
	public boolean isValid(String sessionId, String token) {
		Session sess = map.get(sessionId);
		if (sess == null) {
			return false;
		} else if (sess.token.equals(token) && LocalDateTime.now().isBefore(sess.expireTime)) {
			map.remove(sessionId);
			return true;
		}
		return false;
	}
	
	@Override
	public void close() {
		// nop
	}
	
	private static class Session {
		String token;
		LocalDateTime expireTime;
		
		Session(String token, LocalDateTime expireTime) {
			this.token = token;
			this.expireTime = expireTime;
		}
	}
}
