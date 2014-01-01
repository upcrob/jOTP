package com.upcrob.jotp.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.Client;
import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Controller;
import com.upcrob.jotp.EmailSender;
import com.upcrob.jotp.JsonResponse;
import com.upcrob.jotp.Response;
import com.upcrob.jotp.Sender;
import com.upcrob.jotp.SenderException;
import com.upcrob.jotp.TokenGenerator;
import com.upcrob.jotp.Tokenstore;
import com.upcrob.jotp.TokenstoreException;

/**
 * Describes a Controller that generates and sends one-time password tokens via
 * email.
 * 
 * This controller takes one URL parameter: address - The email address that the
 * generated token should be sent to.
 */
public class EmailOtpController implements Controller {

	private Tokenstore tokenstore;
	private Configuration config;
	private Logger log;
	private Sender sender;

	public EmailOtpController(Configuration config, Tokenstore tokenstore) {
		this.tokenstore = tokenstore;
		this.config = config;
		log = LoggerFactory.getLogger(EmailOtpController.class);
		sender = new EmailSender(config, "Authentication Token");
	}

	@Override
	public Response execute(Map<String, String> params) {
		log.debug("Email OTP request received.");
		
		// Email to send generated token to
		String email = params.get("address");

		// Group information
		String clientName = params.get("client");
		String clientPassword = params.get("clientpassword");

		// Validate client client
		Client client = config.getClients().get(clientName);
		if (clientName == null || client == null) {
			log.debug("Invalid client name: " + clientName);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
		String pwd = client.getPassword();
		if (pwd != null && !pwd.equals(clientPassword)) {
			log.debug("Invalid password: " + clientPassword);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
			
			
		// Validate email
		if (email == null || !email.matches("^[a-zA-Z0-9+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
			log.debug("Invalid email address: " + email);
			return new JsonResponse("ADDR", "No valid email specified.");
		}

		// Generate token
		TokenGenerator tg = TokenGenerator.getInstance();
		String token = tg.getToken(client.getMinOtpLength(),
				client.getMaxOtpLength());

		// Add token to model
		try {
			tokenstore.putToken(clientName, email, token);
		} catch (TokenstoreException e) {
			log.error("Failed to add '" + token + "' to tokenstore.");
			return new JsonResponse("SERV", "Server error.");
		}
		
		// Send token
		try {
			if (!config.isBlockingSmtp()) {
				// Non-blocking SMTP - don't wait for send operation to complete
				// Create a temporary thread for sending the token
				final String tmpEmail = email;
				final String tmpToken = token;
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							log.debug("Attempting to send (non-blocking) token, '"
									+ tmpToken
									+ "' to email address: "
									+ tmpEmail);
							sender.send(tmpEmail, "Your one-time use token: "
									+ tmpToken);
							log.info("Sent token to email address, '"
									+ tmpEmail + "' successfully.");
						} catch (SenderException e) {
							log.error("Failed to send token to, '" + tmpEmail
									+ "'.  Exception was: " + e.getMessage());
						}
					}
				};
				t.start();
			} else {
				// Blocking SMTP - wait for send operation to complete
				log.debug("Attempting to send (smtp blocking) token, '" + token
						+ "' to email address: " + email);
				sender.send(email, "Your one-time use token: " + token);
			}
			log.info("Sent token to email address, '" + email
					+ "' successfully.");
			return new JsonResponse();
		} catch (SenderException e) {
			log.error("Failed to send token to, '" + email
					+ "'.  Exception was: " + e.getMessage());
			return new JsonResponse("SEND", "Could not send token.");
		}
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}
}
