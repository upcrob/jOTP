package org.owasp.jotp;

/**
 * Template for session generation REST requests.
 */
public class SessionGenerationRequest {
	public String email;
	public String subject;
	public String message;
	public int ttl;
}
