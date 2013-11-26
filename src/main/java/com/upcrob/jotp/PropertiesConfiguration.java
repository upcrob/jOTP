package com.upcrob.jotp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Describes a Configuration that is createdd from a Properties file.
 */
public class PropertiesConfiguration implements Configuration {
	
	private Properties props;
	private int otpMinLength;
	private int otpMaxLength;
	private String smtpHost;
	private int smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private Set<String> textProviderHosts;
	private int tokenLifetime;
	
	private PropertiesConfiguration(String path) {
		props = new Properties();
	}
	
	/**
	 * Load configuration from a Properties file.
	 * @param path Path to Properties file.
	 * @throws ConfigurationException Thrown if a configuration error occurs.
	 */
	public static PropertiesConfiguration loadPropertiesConfiguration(String path) throws ConfigurationException {
		
		PropertiesConfiguration config = new PropertiesConfiguration(path);
		try {
			config.props.load(new BufferedReader(new FileReader(path)));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not find configuration file: " + path);
		} catch (IOException e) {
			throw new ConfigurationException("Failed to load configuration file: " + e.getMessage());
		}
		
		Properties props = config.props;
		String p = props.getProperty("MinOtpLength");
		if (p == null || !p.matches("^[1-9][0-9]*$"))
			throw new ConfigurationException("Invalid MinOtpLength. Expecting a positive integer, got: " + p);
		config.otpMinLength = Integer.parseInt(p);
		
		p = props.getProperty("MaxOtpLength");
		if (p == null || !p.matches("^[1-9][0-9]*$"))
			throw new ConfigurationException("Invalid MaxOtpLength. Expecting a positive integer, got: " + p);
		config.otpMaxLength = Integer.parseInt(p);
		if (config.otpMaxLength < config.otpMinLength)
			throw new ConfigurationException("Invalid MaxOtpLength. Should be less than or equal to MinOtpLength.");
		
		config.smtpHost = props.getProperty("SmtpHost");
		if (config.smtpHost == null)
			throw new ConfigurationException("No SmtpHost specified in configuration.");
		
		p = props.getProperty("SmtpPort");
		if (p == null || !p.matches("^[0-9]+$"))
			throw new ConfigurationException("Invalid SmtpPort. Expecting an integer, got: " + p);
		config.smtpPort = Integer.parseInt(p);
		
		config.smtpUsername = props.getProperty("SmtpUsername");
		if (config.smtpUsername == null)
			throw new ConfigurationException("No SmtpUsername specified in configuration.");
		
		config.smtpPassword = props.getProperty("SmtpPassword");
		if (config.smtpPassword == null)
			throw new ConfigurationException("No SmtpPassword specified in configuration.");
		
		p = props.getProperty("TextProviderHosts");
		if (p == null)
			throw new ConfigurationException("No TextProviderHosts specified in configuration.");
		String[] hosts = p.split(",");
		Set<String> pHosts = new HashSet<String>();
		for (String host : hosts) {
			pHosts.add(host);
		}
		config.textProviderHosts = pHosts;
		
		
		p = props.getProperty("TokenLifetime");
		if (p == null || !p.matches("^[0-9]+$"))
			throw new ConfigurationException("Invalid TokenLifetime. Expecting an integer, got: " + p);
		config.tokenLifetime = Integer.parseInt(p);
		
		return config;
	}

	@Override
	public int getOtpMinLength() {
		return otpMinLength;
	}

	@Override
	public int getOtpMaxLength() {
		return otpMaxLength;
	}

	@Override
	public String getSmtpHost() {
		return smtpHost;
	}

	@Override
	public int getSmtpPort() {
		return smtpPort;
	}

	@Override
	public String getSmtpUsername() {
		return smtpUsername;
	}

	@Override
	public String getSmtpPassword() {
		return smtpPassword;
	}

	@Override
	public Set<String> getTextProviderHosts() {
		return textProviderHosts;
	}

	@Override
	public int getTokenLifetime() {
		return tokenLifetime;
	}
}
