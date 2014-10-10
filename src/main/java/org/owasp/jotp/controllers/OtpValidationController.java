package org.owasp.jotp.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.owasp.jotp.Client;
import org.owasp.jotp.Configuration;
import org.owasp.jotp.Controller;
import org.owasp.jotp.JsonResponse;
import org.owasp.jotp.Response;
import org.owasp.jotp.Tokenstore;
import org.owasp.jotp.TokenstoreException;

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

	private Tokenstore tokenstore;
	private Configuration config;
	private Logger log;
	
	public OtpValidationController(Configuration config, Tokenstore tokenstore) {
		this.config = config;
		this.tokenstore = tokenstore;
		log = LoggerFactory.getLogger(OtpValidationController.class);
	}
	
	@Override
	public Response execute(Map<String, String> params) {
		// Get input from request
		String uid = params.get("uid");
		String tokenString = params.get("token");
		String clientName = params.get("client");
		String clientPassword = params.get("clientpassword");
		Client client = config.getClients().get(clientName);
		
		// Check client
		if (clientName == null || client == null) {
			log.debug("Invalid client name: {}", client);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
		String pwd = client.getPassword();
		if (pwd != null && !pwd.equals(clientPassword)) {
			log.debug("Invalid client password: {}", pwd);
			return new JsonResponse("GROUP", "Invalid client name or password.");
		}
		
		// Check uid and token parameters
		if (uid == null) {
			return new JsonResponse("NO_UID", "No user identifier (uid) specified.");
		} else if (tokenString == null) {
			return new JsonResponse("NO_TOKEN", "No token specified.");
		} else {
			try {
				if (tokenstore.isTokenValid(clientName, uid, tokenString)) {
					// Token was valid
					JsonResponse resp = new JsonResponse();
					resp.setField("tokenValid", "true");
					log.info("Token for UID, '" + uid + "' was validated successfully.");
					return resp;
				} else {
					// Token wasn't valid
					JsonResponse resp = new JsonResponse();
					resp.setField("tokenValid", "false");
					log.info("Token for UID, '"
							+ uid
							+ "' was not valid.  Token attempted was: "
							+ tokenString);
					return resp;
				}
			} catch (TokenstoreException e) {
				log.error("Failed to validate token with tokenstore.");
				return new JsonResponse("SERV", "Server error.");
			}
		}
	}

}
