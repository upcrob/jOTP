package org.owasp.jotp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reaper thread that is executed every 60 seconds
 * to remove expired one-time passwords in order
 * to keep allocated JVM memory from expanding
 * over time.
 */
public class Reaper extends Thread {
	// Time interval (in millis) between cleaning cycles
	private static final int CLEANING_INTERVAL = 60000;
	
	// Reference to the Tokenstore object
	private Tokenstore tokenstore;
	
	public Reaper(Tokenstore tokenstore) {
		this.tokenstore = tokenstore;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		Logger log = LoggerFactory.getLogger(Reaper.class);
		
		while (true) {
			// Clean the model
			log.debug("Starting reaper cleaning cycle.");
			try {
				tokenstore.removeExpired();
				log.info("Reaper cycle complete.");
			} catch (TokenstoreException e) {
				log.error("Could not complete reaper cleaning cycle.");
			}
			
			// Sleep the thread
			try {
				Thread.sleep(CLEANING_INTERVAL);
			} catch (InterruptedException e) {
				// Don't do anything
			}
		}
	}
}
