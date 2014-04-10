package org.owasp.jotp.controllers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.owasp.jotp.Client;
import org.owasp.jotp.Configuration;
import org.owasp.jotp.Controller;
import org.owasp.jotp.EmailSender;
import org.owasp.jotp.JsonResponse;
import org.owasp.jotp.Response;
import org.owasp.jotp.Sender;
import org.owasp.jotp.SenderException;
import org.owasp.jotp.TokenGenerator;
import org.owasp.jotp.Tokenstore;
import org.owasp.jotp.TokenstoreException;

/**
 * Describes a Controller that generates and sends one-time
 * password tokens via text message.
 * 
 * This controller takes one URL parameter:
 * number - The phone number that the one-time password token
 *   should be sent to.
 */
public class TextOtpController implements Controller {
	
	private Tokenstore tokenstore;
	private Configuration config;
	private Logger log;
	private Sender sender;
	
	public TextOtpController(Configuration config, Tokenstore tokenstore) {
		this.tokenstore = tokenstore;
		this.config = config;
		log = LoggerFactory.getLogger(TextOtpController.class);
		sender = new EmailSender(config, "Authentication Token");
	}
	
	@Override
	public Response execute(Map<String, String> params) {
		log.debug("Text OTP request received.");
		
		// Phone number to send generated token to.  Must not be null.
		String phone = params.get("number");
		
		// Client client information
		String clientName = params.get("client");
		String clientPassword = params.get("clientpassword");
		
		// Validate client
		Client client = config.getClients().get(clientName);
		if (client == null) {
			log.debug("Invalid client name: " + clientName);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
		String pwd = client.getPassword();
		if (pwd != null && !pwd.equals(clientPassword)) {
			log.debug("Invalid client password: " + pwd);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
		
		// Validate phone number
		if (phone == null || !phone.matches("^(\\d{3})-?(\\d{3})-?(\\d{4})$")) {
			log.debug("Invalid phone number: " + phone);
			return new JsonResponse("NO_PHONE", "No valid phone number specified.");
		}
		
		// Phone number was valid
		// Generate token
		TokenGenerator tg = TokenGenerator.getInstance();
		String token = tg.getToken(client.getMinOtpLength(), client.getMaxOtpLength());
		
		// Add token to model
		try {
			tokenstore.putToken(clientName, phone, token);
		} catch (TokenstoreException e) {
			log.error("Failed to add '" + token + "' to tokenstore.");
			return new JsonResponse("SERV", "Server error.");
		}
		
		// Send token
		try {
			if (!config.isBlockingSmtp()) {
				// Non-blocking SMTP - don't wait for send operation to complete
				// Create a temporary thread for sending the token
				final String tmpToken = token;
				final String tmpPhone = phone;
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							log.debug("Attempting to send (non-blocking) token, '"
									+ tmpToken
									+ "' to phone number: "
									+ tmpPhone);
							sendToken(tmpToken, tmpPhone);
							log.info("Sent token to phone, '"
									+ tmpPhone
									+ "' successfully.");
						} catch (SenderException e) {
							log.error("Failed to send token to '"
									+ tmpPhone
									+ "'.  Exception was: "
									+ e.getMessage());
						}
					}
				};
				t.start();
			} else {
				// Blocking SMTP - wait for send operation to complete
				log.debug("Attempting to send (smtp blocking) token, '"
						+ token
						+ "' to phone number: "
						+ phone);
				sendToken(token, phone);
				log.info("Sent token to phone, '" + phone + "' successfully.");
			}
			
			return new JsonResponse();
		} catch (SenderException e) {
			log.error("Failed to send token to '"
					+ phone
					+ "'.  Exception was: "
					+ e.getMessage());
			return new JsonResponse("SEND", "Could not send token.");
		}
	}
	
	private void sendToken(String token, String number) throws SenderException {
		// Build recipient list
		Set<String> hosts = config.getMobileProviderHosts();
		Set<String> recipients = new HashSet<String>();
		for (String host : hosts) {
			log.debug("Adding '" + number + "@" + host + "' to send list.");
			recipients.add(number + "@" + host);
		}

		// Send
		sender.send(recipients, token);
	}
	
	public void setSender(Sender sender) {
		this.sender = sender;
	}
}
