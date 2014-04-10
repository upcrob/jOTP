package org.owasp.jotp;


import java.util.Map;

/**
 * Describes a controller that can be called from
 * the main servlet.  Controllers should have a one-to-one
 * correspondence with URLs.
 */
public interface Controller {
	/**
	 * Executes the controller action with the given parameters.
	 */
	public Response execute(Map<String, String> parameters);
}
