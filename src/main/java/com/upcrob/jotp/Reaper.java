package com.upcrob.jotp;

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
	
	// Reference to the model object
	private Model model;
	
	public Reaper(Model model) {
		this.model = model;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		Logger log = LoggerFactory.getLogger(Reaper.class);
		
		while (true) {
			// Clean the model
			log.debug("Starting reaper cleaning cycle.");
			model.removeExpired();
			log.debug("Reaper cycle complete.");
			
			// Sleep the thread
			try {
				Thread.sleep(CLEANING_INTERVAL);
			} catch (InterruptedException e) {
				// Don't do anything
			}
		}
	}
}
