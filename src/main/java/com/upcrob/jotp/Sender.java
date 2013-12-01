package com.upcrob.jotp;

import java.util.Set;

/**
 * Describes an algorithm for sending a message.
 */
public interface Sender {
	/**
	 * Invoke the send algorithm.
	 * @param recipient The recipient address.
	 * @param message The content of the message to send.
	 */
	public void send(String recipient, String message) throws SenderException;
	
	/**
	 * Invoke the send algorithm.
	 * @param recipients Array of recipient addresses.
	 * @param message The content of the message to send.
	 */
	public void send(Set<String> recipients, String message) throws SenderException;
}
