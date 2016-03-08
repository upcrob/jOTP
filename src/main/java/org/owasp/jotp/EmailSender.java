package org.owasp.jotp;

import java.util.Properties;

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
 * Sends a message to the given email address.
 */
public class EmailSender {

	private final String host;
	private final int port;
	private final String fromAddr;
	private final boolean useTls;
	private final String username;
	private final String password;
	
	public EmailSender(String host, int port, String fromAddr, boolean useTls) {
		this(host, port, fromAddr, useTls, null, null);
	}
	
	public EmailSender(String host, int port, String fromAddr, boolean useTls, String username, String password) {
		this.host = host;
		this.port = port;
		this.useTls = useTls;
		this.fromAddr = fromAddr;
		this.username = username;
		this.password = password;
	}

	public void send(String emailAddr, String subject, String message) throws ServiceException {
		Address[] addresses = new Address[1];
		try {
			addresses[0] = new InternetAddress(emailAddr);
		} catch (AddressException e) {
			throw new ServiceException("Invalid email address.", e);
		}
		
		Properties props = new Properties();
		// Set host and port
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		
		// Determine if TLS should be enabled
		if (useTls) {
			props.put("mail.smtp.starttls.enable", "true");
		}
			
		// Determine if authentication is required
		Session sess;
		if (username != null && password != null) {
			// Use username / password authentication
			props.put("mail.smtp.auth", "true");
			sess = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
			});
		} else {
			// Get default instance, no authentication
			sess = Session.getDefaultInstance(props);
		}
		
		// Send message
		Message msg = new MimeMessage(sess);
		try {
			msg.setFrom(new InternetAddress(fromAddr));
			msg.setRecipients(Message.RecipientType.TO, addresses);
			msg.setSubject(subject);
			msg.setText(message);
			Transport.send(msg);
		} catch (MessagingException e) {
			throw new ServiceException("An error occurred while sending the message.", e);
		}
	}
}
