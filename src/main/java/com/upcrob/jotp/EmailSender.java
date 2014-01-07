package com.upcrob.jotp;

import java.util.HashSet;
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

/**
 * Send a message to the given email address.
 */
public class EmailSender implements Sender {

	private Configuration config;
	private String subject;
	
	public EmailSender(Configuration config, String subject) {
		this.config = config;
		this.subject = subject;
	}
	
	@Override
	public void send(String addr, String message) throws SenderException {
		Set<String> recipients = new HashSet<String>();
		recipients.add(addr);
		send(recipients, message);
	}

	@Override
	public void send(Set<String> recipients, String message)
			throws SenderException {
		Address[] addresses = new Address[recipients.size()];
		int i = 0;
		for (String recipient : recipients) {
			try {
				addresses[i++] = new InternetAddress(recipient);
			} catch (AddressException e) {
				throw new SenderException("Invalid email address: "
					+ recipient, e);
			}
		}
		
		Properties props = new Properties();
		// Set host and port
		props.put("mail.smtp.host", config.getSmtpHost());
		props.put("mail.smtp.port", config.getSmtpPort());
		
		// Determine if TLS should be enabled
		if (config.isSmtpTls())
			props.put("mail.smtp.starttls.enable", "true");
		
		// Determine if authentication is required
		Session sess;
		if (config.getSmtpAuthType() == AuthType.PASSWORD) {
			// Use username / password authentication
			props.put("mail.smtp.auth", "true");
			sess = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(config.getSmtpUsername(), 
								config.getSmtpPassword());
					}
			});
		} else {
			// Get default instance, no authentication
			sess = Session.getDefaultInstance(props);
		}
		
		// Send message
		Message msg = new MimeMessage(sess);
		try {
			msg.setFrom(new InternetAddress(config.getSmtpFrom()));
			msg.setRecipients(Message.RecipientType.TO, addresses);
			msg.setSubject(subject);
			msg.setText(message);
			Transport.send(msg);
		} catch (MessagingException e) {
			throw new SenderException("An error occurred while sending the message: "
				+ e.getMessage(), e);
		}
	}

}
