package com.upcrob.jotp;


import javax.servlet.http.HttpServletRequest;

/**
 * Describes a controller that can be called from
 * the main servlet.  Controllers should have a one-to-one
 * correspondence with URLs.
 */
public interface Controller {
	/**
	 * Executes the controller action with the given request object and
	 * returns a response String.
	 */
	public String execute(HttpServletRequest request);
}
