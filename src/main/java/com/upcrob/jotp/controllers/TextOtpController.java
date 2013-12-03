package com.upcrob.jotp.controllers;

import java.util.HashSet;
import java.util.Set;

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
 * Describes a Controller that generates and sends one-time
 * password tokens via text message.
 * 
 * This controller takes one URL parameter:
 * number - The phone number that the one-time password token
 *   should be sent to.
 */
public class TextOtpController implements Controller {
	
	private Model model;
	private Configuration config;
	private Logger log;
	private Sender sender;
	
	public TextOtpController(Configuration config, Model model) {
		this.model = model;
		this.config = config;
		log = LoggerFactory.getLogger(TextOtpController.class);
		sender = new EmailSender(config, "Authentication Token");
	}
	
	@Override
	public String execute(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		
		// Phone number to send generated token to.  Must not be null.
		String phone = request.getParameter("number");
				
		// Validate input
		if (phone == null
				|| !phone.matches("^(\\d{3})-?(\\d{3})-?(\\d{4})$")) {
			sb.append("{\"error\":\"No valid phone number specified.\"}");
		} else {
			// Phone number was valid
			// Generate token
			TokenGenerator tg = TokenGenerator.getInstance();
			String token = tg.getToken(config.getOtpMinLength(), config.getOtpMaxLength());
			
			// Add token to model
			model.putToken(phone, token, config.getTokenLifetime());
			
			// Send token
			try {
				if (config.isOptimisticResponse()) {
					// Optimistic response - don't wait for send operation to complete
					// Create a temporary thread for sending the token
					final String tmpToken = token;
					final String tmpPhone = phone;
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								log.debug("Attempting to send token, '"
										+ tmpToken
										+ "' optimistically to phone number: "
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
					// Non-optimistic response - wait for send operation to complete
					log.debug("Attempting to send token, '"
							+ token
							+ "' non-optimistically to phone number: "
							+ phone);
					sendToken(token, phone);
					log.info("Sent token to phone, '" + phone + "' successfully.");
				}
				
				sb.append("{\"error:\": \"\"}");
			} catch (SenderException e) {
				log.error("Failed to send token to '"
						+ phone
						+ "'.  Exception was: "
						+ e.getMessage());
				sb.append("{\"error\": \"Could not send token.\"}");
			}
		}
		
		return sb.toString();
	}
	
	private void sendToken(String token, String number) throws SenderException {
		// Build recipient list
		Set<String> hosts = config.getMobileProviderHosts();
		Set<String> recipients = new HashSet<String>();
		for (String host : hosts) {
			recipients.add(number + "@" + host);
		}

		// Send
		sender.send(recipients, "Token: " + token);
	}
	
	public void setSender(Sender sender) {
		this.sender = sender;
	}
}
