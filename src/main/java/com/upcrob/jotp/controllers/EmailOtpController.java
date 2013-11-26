package com.upcrob.jotp.controllers;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Controller;
import com.upcrob.jotp.Model;
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
	
	public EmailOtpController(Configuration config, Model model) {
		this.model = model;
		this.config = config;
		log = LoggerFactory.getLogger(EmailOtpController.class);
	}
	
	@Override
	public String execute(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		
		// Email to send generated token to
		String email = request.getParameter("address");

		// Validate input
		if (email == null || !email.matches("^[a-zA-Z0-9+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
			sb.append("{\"error\":\"No valid email specified.\"}");
		} else {
			// Phone number was valid
			// Generate token
			TokenGenerator tg = TokenGenerator.getInstance();
			String token = tg.getToken(config.getOtpMinLength(), config.getOtpMaxLength());

			// Add token to model
			model.putToken(email, token, config.getTokenLifetime());

			// Send token
			try {
				log.debug("Attempting to send token, '" + token
						+ "' to email address: " + email);
				sendToken(token, email);
				log.info("Sent token to email address, '" + email
						+ "' successfully.");
				sb.append("{\"error:\": \"\"}");
			} catch (MessagingException e) {
				log.error("Failed to send token to, '" + email
						+ "'.  Exception was: " + e.getMessage());
				sb.append("{\"error\": \"Could not send token.\"}");
			}
		}

		return sb.toString();
	}

	private void sendToken(String token, String addr) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", config.getSmtpHost());
		props.put("mail.smtp.port", config.getSmtpPort());
		Session sess = Session.getInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
				}
		});
		
		Message msg = new MimeMessage(sess);
		msg.setFrom(new InternetAddress(config.getSmtpUsername()));
		Address recipient = new InternetAddress(addr);
		msg.setRecipient(Message.RecipientType.TO, recipient);
		msg.setSubject("Authentication Token");
		msg.setText("Your one-time use token: " + token);
		Transport.send(msg);
	}
}
