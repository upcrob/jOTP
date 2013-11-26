package com.upcrob.jotp;

/**
 * Describes an exception thrown due to an invalid configuration.
 * 
 * This may include invalid values in a configuration file, or
 * parse errors.
 */
public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 2318350303076767620L;
	
	public ConfigurationException(String msg) {
		super(msg);
	}
}
