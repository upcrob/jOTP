package com.upcrob.jotp.controllers;

import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
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
	
	public TextOtpController(Configuration config, Model model) {
		this.model = model;
		this.config = config;
		log = LoggerFactory.getLogger(TextOtpController.class);
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
				log.debug("Attempting to send token, '" + token + "' to: " + phone);
				sendToken(token, phone);
				log.info("Sent token to phone, '" + phone + "' successfully.");
				sb.append("{\"error:\": \"\"}");
			} catch (AddressException e) {
				log.error("Failed to send token to '"
						+ phone
						+ "'.  Exception was: "
						+ e.getMessage());
				sb.append("{\"error\": \"Could not send token.\"}");
			} catch (MessagingException e) {
				log.error("Failed to send token to '"
						+ phone
						+ "'.  Exception was: "
						+ e.getMessage());
				sb.append("{\"error\": \"Could not send token.\"}");
			}
		}
		
		return sb.toString();
	}
	
	private void sendToken(String token, String number) throws AddressException, MessagingException {
		// Build recipient list
		Set<String> hosts = config.getTextProviderHosts();
		Address[] addresses = new Address[hosts.size()];
		int i = 0;
		for (String host : hosts) {
			String addr = null;
			try {
				addr = number + "@" + host;
				addresses[i++] = new InternetAddress(addr);
			} catch (AddressException e) {
				throw new AddressException("Invalid recipient address: " + addr
						+ " | " + e.getMessage());
			}
		}
		
		
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
		
		try {
			Message msg = new MimeMessage(sess);
			msg.setFrom(new InternetAddress(config.getSmtpUsername()));
			msg.setRecipients(Message.RecipientType.TO, addresses);
			msg.setSubject("");
			msg.setText("Token: " + token);
			Transport.send(msg);
		} catch (AddressException e) {
			throw new AddressException("Invalid FROM address: "
					+ config.getSmtpUsername()
					+ " | "
					+ e.getMessage());
		}
	}
}
