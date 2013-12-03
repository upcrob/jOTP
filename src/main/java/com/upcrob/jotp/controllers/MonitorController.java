package com.upcrob.jotp.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.Controller;

/**
 * Simple endpoint that can be used to verify that the application
 * is currently available.
 */
public class MonitorController implements Controller {

	private Logger log;
	
	public MonitorController() {
		log = LoggerFactory.getLogger(MonitorController.class);
	}
	
	@Override
	public String execute(HttpServletRequest request) {
		log.debug("Monitored - OK");
		return "{\"status\": \"OK\"}";
	}

}
