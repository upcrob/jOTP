package com.upcrob.jotp.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.Controller;
import com.upcrob.jotp.Model;

/**
 * Describes a controller that checks to see whether or not an
 * input one-time password token is valid for a corresponding
 * user ID (email, phone number, etc).
 * 
 * Note that in the case of an invalid token, details explaining
 * the cause (invalid user ID, valid user ID but invalid token,
 * etc) are not provided in order to prevent state information
 * leakage.
 * 
 * The controller takes two parameters from the URL:
 * uid - The user ID associated with a one-time password.  This
 *   corresponds to the user ID used when the one-time password
 *   was originally generated (eg. email address, phone number, etc).
 * token - The one-time password token submitted by the user.
 */
public class OtpValidationController implements Controller {

	private Model model;
	
	private Logger log;
	
	public OtpValidationController(Model model) {
		this.model = model;
		log = LoggerFactory.getLogger(OtpValidationController.class);
	}
	
	@Override
	public String execute(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		
		// Get input from request
		String uid = request.getParameter("uid");
		String tokenString = request.getParameter("token");
		
		if (uid == null) {
			sb.append("{\"error\": \"No user identifier (uid) specified.\"}");
		} else if (tokenString == null) {
			sb.append("{\"error\": \"No token specified.\"}");
		} else {
			if (model.isTokenValid(uid, tokenString)) {
				// Token was valid, invalidate it and return response
				model.removeToken(uid);
				sb.append("{\"error\": \"\", \"tokenValid\": \"true\"}");
				log.info("Token for UID, '" + uid + "' was validated successfully.");
			} else {
				// Token wasn't valid
				sb.append("{\"error\": \"\", \"tokenValid\": \"false\"}");
				log.info("Token for UID, '"
						+ uid
						+ "' was not valid.  Token attempted was: "
						+ tokenString);
			}
		}
		
		return sb.toString();
	}

}
