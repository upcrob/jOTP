package com.upcrob.jotp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 * Described a configuration loaded from YAML format.
 */
public class YamlConfiguration implements Configuration {

	private Logger log;
	private String smtpHost;
	private int smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private Set<String> mobileProviderHosts;
	private boolean blockingSmtp;
	private Map<String, Client> clients;
	private TokenstoreType tokenstoreType;
	private String jdbcString;
	private String redisHost;
	private int redisPort;
	private String redisPassword;
	
	public YamlConfiguration(String path) throws ConfigurationException, FileNotFoundException {
		this(new File(path));
	}
	
	public YamlConfiguration(File file) throws ConfigurationException, FileNotFoundException {
		this(new FileReader(file));
	}
	
	@SuppressWarnings("unchecked")
	public YamlConfiguration(Reader reader) throws ConfigurationException {
		// Setup logger
		log = LoggerFactory.getLogger(YamlConfiguration.class);
		
		// Set default values for optional parameters
		blockingSmtp = false;
		jdbcString = null;
		redisPort = -1;
		redisPassword = null;
		
		// Get root map
		Yaml yaml = new Yaml();
		
		Map<Object, Object> map;
		try {
			map = (Map<Object, Object>) yaml.load(reader);
		} catch (ScannerException e) {
			throw new ConfigurationException("Parse error: " + e.getMessage());
		}
		
		// Load SmtpHost
		Object o;
		String s;
		List<Object> l;
		o = map.get("SmtpHost");
		if (o == null)
			throw new ConfigurationException("No SmtpHost specified in configuration.");
		smtpHost = o.toString();
		log.debug("SMTP host: " + smtpHost);
		
		// Load SmtpPort
		o = map.get("SmtpPort");
		if (o == null)
			throw new ConfigurationException("No SmtpPort specified in configuration.");
		if (!isInteger(o))
			throw new ConfigurationException("SmtpPort must be a valid integer.");
		smtpPort = Integer.parseInt(o.toString());
		log.debug("SMTP port: " + smtpPort);
		
		// Load SmtpUsername
		o = map.get("SmtpUsername");
		if (o == null)
			throw new ConfigurationException("No SmtpUsername specified in configuration.");
		smtpUsername = o.toString();
		log.debug("SMTP username: " + smtpUsername);
		
		// Load SmtpPassword
		o = map.get("SmtpPassword");
		if (o == null)
			throw new ConfigurationException("No SmtpPassword specified in configuration.");
		smtpPassword = o.toString();
		log.debug("SMTP password: " + smtpPassword);
		
		// Load BlockingSmtp
		o = map.get("BlockingSmtp");
		if (o != null) {
			s = o.toString();
			if ("true".equalsIgnoreCase(s))
				blockingSmtp = true;
			else if ("false".equalsIgnoreCase(s))
				blockingSmtp = false;
			else
				throw new ConfigurationException("Value of BlockingSmtp must be 'true' or 'false'.");
		}
		log.debug("Blocking SMTP: " + blockingSmtp);
		
		// Load Tokenstore type
		o = map.get("TokenstoreType");
		if (o == null || TokenstoreType.LOCAL.toString().equals(o.toString())) {
			// Use in-memory store
			log.debug("Using LocalTokenstore");
			tokenstoreType = TokenstoreType.LOCAL;
			jdbcString = null;
		} else if (TokenstoreType.JDBC.toString().equals(o.toString())) {
			// JDBC Tokenstore
			tokenstoreType = TokenstoreType.JDBC;
			log.debug("Using JDBC Tokenstore");
			o = map.get("JdbcString");
			if (o == null)
				throw new ConfigurationException("No JDBC connection string specified.");
			jdbcString = o.toString();
			log.debug("JDBC connection string: " + jdbcString);
			o = map.get("JdbcDriver");
			if (o == null)
				throw new ConfigurationException("No JDBC driver class specified.");
			String jdbcDriver = o.toString();
			log.debug("JDBC driver: " + jdbcDriver);
			try {
				// Load JDBC driver
				Class.forName(jdbcDriver);
				log.info(jdbcDriver + " loaded successfully.");
			} catch (ClassNotFoundException e) {
				throw new ConfigurationException("JDBC driver not found on classpath: " + o);
			}
		} else if (TokenstoreType.REDIS.toString().equals(o.toString())) {
			// Redis Tokenstore
			tokenstoreType = TokenstoreType.REDIS;
			log.debug("Using Redis Tokenstore");
			
			// RedisHost
			o = map.get("RedisHost");
			if (o == null)
				throw new ConfigurationException("No RedisHost specified.");
			redisHost = o.toString();
			log.debug("Redis host: " + redisHost);
			
			// RedisPort
			o = map.get("RedisPort");
			if (o != null) {
				if (!isInteger(o))
					throw new ConfigurationException("RedisPort must be an integer.");
				redisPort = Integer.parseInt(o.toString());
				log.debug("Redis port: " + redisPort);
			} else {
				log.debug("No Redis port set.  Using the default port.");
			}
			
			// RedisPassword
			o = map.get("RedisPassword");
			if (o != null) {
				redisPassword = o.toString();
				log.debug("Redis password: " + redisPassword);
			} else {
				log.debug("No Redis password set.");
			}
		} else {
			// Unknown Tokenstore type
			throw new ConfigurationException("Unknown TokenstoreType: " + o.toString());
		}
		
		// Load MobileProviderHosts
		l = (List<Object>) map.get("MobileProviderHosts");
		mobileProviderHosts = new HashSet<String>();
		if (l == null)
			throw new ConfigurationException("No MobileProviderHosts specified in configuration.");
		for (Object h : l) {
			s = h.toString();
			mobileProviderHosts.add(s.trim());
			log.debug("Mobile provided host added: " + s);
		}
		
		// Load Clients
		clients = new HashMap<String, Client>();
		List<Map<Object, Object>> clientList = (List<Map<Object, Object>>) map.get("Clients");
		if (clientList == null)
			throw new ConfigurationException("No clients specified in configuration.");
		for (Map<Object, Object> client : clientList) {
			Client g = new Client();
			Object name = client.get("Name");
			Object password = client.get("Password");
			Object minLength = client.get("MinOtpLength");
			Object maxLength = client.get("MaxOtpLength");
			Object lifetime = client.get("TokenLifetime");
			
			if (name == null)
				throw new ConfigurationException("Each client must have a name.");
			log.debug("Adding client: " + name);
			if (password != null) {
				g.setPassword(password.toString());
				log.debug("Set client password to: " + password);
			} else {
				log.debug("No password set for client: " + name);
			}
			
			try {
				if (minLength != null) {
					g.setMinOtpLength(Integer.parseInt(minLength.toString()));
					log.debug("Set minimum OTP string length to: " + minLength);
				}
				if (maxLength != null) {
					g.setMaxOtpLength(Integer.parseInt(maxLength.toString()));
					log.debug("Set maximum OTP string length to: " + maxLength);
				}
				if (lifetime != null) {
					g.setTokenLifetime(Integer.parseInt(lifetime.toString()));
					log.debug("Set maximum token lifetime (in seconds) to: " + lifetime);
				}
			} catch (NumberFormatException e) {
				throw new ConfigurationException("MinOtpLength, MaxOtpLength, and TokenLifetime must be integer values.");
			}
			clients.put(name.toString(), g);
		}
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
	public Set<String> getMobileProviderHosts() {
		return mobileProviderHosts;
	}

	@Override
	public boolean isBlockingSmtp() {
		return blockingSmtp;
	}

	@Override
	public Map<String, Client> getClients() {
		return clients;
	}

	private static boolean isInteger(Object o) {
		if (o == null)
			return false;
		String s = o.toString();
		return s.matches("^[0-9]+$");
	}

	@Override
	public TokenstoreType getTokenstoreType() {
		return tokenstoreType;
	}

	@Override
	public String getJdbcString() {
		return jdbcString;
	}
	
	@Override
	public String getRedisHost() {
		return redisHost;
	}
	
	@Override
	public int getRedisPort() {
		return redisPort;
	}
	
	@Override
	public String getRedisPassword() {
		return redisPassword;
	}
}
