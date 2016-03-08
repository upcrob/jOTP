package org.owasp.jotp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Spark;

import com.google.gson.*;

import static spark.Spark.*;

/**
 * jOTP driver class.
 */
public class Main {
	
	private static final Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static SessionRepository repository;
	private static EmailSender emailSender;
	private static String baseUrl;
	
	private static Map<String, String> properties;
	
	public static void main(String[] args) {
		// load property files
		String propertyFiles = System.getProperty("propertyfiles");
		if (propertyFiles == null || "".equals(propertyFiles.trim())) {
			System.err.println("No property files specified.  Set the 'propertyfiles' property for the VM.");
			System.exit(1);
		}
		properties = new HashMap<>();
		String[] files = propertyFiles.split(",");
		for (String file : files) {
			Properties props = new Properties();
			try {
				props.load(new FileReader(new File(file)));
				for (Object k : props.keySet()) {
					properties.put(k.toString(), props.get(k).toString());
				}
			} catch (IOException e) {
				System.err.println("Failed to load property file: "
						+ file + "  " + e.getMessage());
				System.exit(1);
			}
		}
		
		// configure
		int port = getPropertyAsInt("http.port", true);
		baseUrl = getPropertyAsString("base.url", true);
		String smtpHost = getPropertyAsString("smtp.host", true);
		int smtpPort = getPropertyAsInt("smtp.port", true);
		String smtpFrom = getPropertyAsString("smtp.from", true);
		boolean smtpTls = getPropertyAsBoolean("smtp.tls", true);
		
		// configure repository
		String repoType = getPropertyAsString("repository.type", false);
		if ("redis".equals(repoType)) {
			logger.info("Configuring Redis repository.");
			repository = new RedisSessionRepository(getPropertyAsString("redis.host", true),
					getPropertyAsInt("redis.port", true),
					getPropertyAsString("redis.creds", false));
		} else if ("jdbc".equals(repoType)) {
			logger.info("Configuring JDBC repository.");
			repository = new JdbcSessionRepository(getPropertyAsString("jdbc.url", true),
					getPropertyAsString("jdbc.table", true));
		} else {
			logger.warn("Using local in-memory repository.  This is not recommended for production environments.");
			repository = new InMemorySessionRepository();
		}
		
		// configure sender
		String smtpUsername = getPropertyAsString("smtp.username", false);
		String smtpPassword = getPropertyAsString("smtp.password", false);
		if (smtpUsername != null && smtpPassword != null) {
			emailSender = new EmailSender(smtpHost, smtpPort, smtpFrom, smtpTls, smtpUsername, smtpPassword);
		} else {
			emailSender = new EmailSender(smtpHost, smtpPort, smtpFrom, smtpTls);
		}
		
		// configure shutdown hook to close repository
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down web server...");
				Spark.stop();
				logger.info("Server stopped.");
				logger.info("Shutting down repository...");
				repository.close();
				logger.info("Shutdown complete.");
			}
		});
		
		// configure http server
		port(port);
		post("/sessions", (req, res) -> handleTokenGenerationRequest(req, res));
		post("/sessions/:session", (req, res) -> handleTokenValidationRequest(req, res));
		get("/", (req, res) -> handleMonitorRequest(req, res));
	}

	private static String handleTokenGenerationRequest(Request req, Response res) {
		SessionGenerationRequest sessReq = gson.fromJson(req.body(), SessionGenerationRequest.class);
		
		String token = randomString().substring(0, 5).toUpperCase();
		String session = randomString().substring(0, 16);
		try {
			repository.createSession(session, token, LocalDateTime.now().plusMinutes(sessReq.ttl));
			emailSender.send(sessReq.email, sessReq.subject, replaceMessageVars(sessReq.message, session, token));
			res.status(201);
			res.header("Location", baseUrl + "/sessions/" + session);
			return "";
		} catch (ServiceException e) {
			logger.error("Session repository error during session creation.", e);
			res.status(500);
			return "Internal Server Error";
		}
	}
	
	private static String handleTokenValidationRequest(Request req, Response res) {
		String session = req.params(":session");
		String token = req.body();
		try {
			if (repository.isValid(session, token)) {
				res.status(200);
			} else {
				res.status(404);
			}
		} catch (ServiceException e) {
			res.status(500);
			logger.error("Session repository error during validation.", e);
		}
		
		return "";
	}

	private static String handleMonitorRequest(Request req, Response res) {
		res.status(200);
		return "OK";
	}
	
	private static String getPropertyAsString(String name, boolean required) {
		String value = properties.get(name);
		if (value == null) {
			if (required)
				throw new IllegalArgumentException("Missing configuration property: " + name);
		}
		logger.info("Got configuration property: " + name);
		return value;
	}
	
	private static int getPropertyAsInt(String name, boolean required) {
		String value = properties.get(name);
		if (value == null) {
			if (required)
				throw new IllegalArgumentException("Missing configuration property: " + name);
		} else if (!value.matches("[0-9]+")) {
			throw new IllegalArgumentException("Invalid value for configuration property: " + name);
		}
		logger.info("Got configuration property: " + name);
		return Integer.parseInt(value);
	}
	
	private static boolean getPropertyAsBoolean(String name, boolean required) {
		String value = properties.get(name);
		if (value == null) {
			if (required)
				throw new IllegalArgumentException("Missing configuration property: " + name);
		}
		boolean boolValue = Boolean.valueOf(value).booleanValue();
		logger.info("Got configuration property: " + name);
		return boolValue;
	}
	
	private static String randomString() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replaceAll("-", "");
	}
	
	private static String replaceMessageVars(String message, String session, String token) {
		return message.replace("$session", session).replace("$token", token);
	}
}
