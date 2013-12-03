package com.upcrob.jotp.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Controller;
import com.upcrob.jotp.EmailSender;
import com.upcrob.jotp.Model;
import com.upcrob.jotp.Sender;
import com.upcrob.jotp.SenderException;
import com.upcrob.jotp.TokenGenerator;

/**
 * Describes a Controller that generates and sends one-time password
 * tokens via email.
 * 
 * This controller takes one URL parameter:
 * address - The email address that the generated token should be 
 * sent to.
 */
public class EmailOtpController implements Controller {

	private Model model;
	private Configuration config;
	private Logger log;
	private Sender sender;
	
	public EmailOtpController(Configuration config, Model model) {
		this.model = model;
		this.config = config;
		log = LoggerFactory.getLogger(EmailOtpController.class);
		sender = new EmailSender(config, "Authentication Token");
	}
	
	@Override
	public String execute(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		
		// Email to send generated token to
		String email = request.getParameter("address");

		// Validate input
		if (email == null || !email.matches("^[a-zA-Z0-9+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
			sb.append("{\"error\": \"No valid email specified.\"}");
		} else {
			// Phone number was valid
			// Generate token
			TokenGenerator tg = TokenGenerator.getInstance();
			String token = tg.getToken(config.getOtpMinLength(), config.getOtpMaxLength());

			// Add token to model
			model.putToken(email, token, config.getTokenLifetime());

			// Send token
			try {
				if (config.isOptimisticResponse()) {
					// Optimistic response - don't wait for send operation to complete
					// Create a temporary thread for sending the token
					final String tmpEmail = email;
					final String tmpToken = token;
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								log.debug("Attempting to send token, '"
										+ tmpToken
										+ "' optimistically to email address: "
										+ tmpEmail);
								sender.send(tmpEmail, "Your one-time use token: "
										+ tmpToken);
								log.info("Sent token to email address, '"
									+ tmpEmail
									+ "' successfully.");
							} catch (SenderException e) {
								model.removeToken(tmpEmail);
								log.error("Failed to send token to, '" + tmpEmail
										+ "'.  Exception was: " + e.getMessage());
							}
						}
					};
					t.start();
				} else {
					// Non-optimistic response - wait for send operation to complete
					log.debug("Attempting to send token, '" + token
							+ "' non-optimistically to email address: " + email);
					sender.send(email, "Your one-time use token: " + token);
				}
				log.info("Sent token to email address, '" + email
						+ "' successfully.");
				sb.append("{\"error:\": \"\"}");
			} catch (SenderException e) {
				model.removeToken(email);
				log.error("Failed to send token to, '" + email
						+ "'.  Exception was: " + e.getMessage());
				sb.append("{\"error\": \"Could not send token.\"}");
			}
		}

		return sb.toString();
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}
}
