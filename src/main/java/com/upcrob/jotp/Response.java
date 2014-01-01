package com.upcrob.jotp;

/**
 * Describes the standard response returned
 * from the REST endpoints.
 * 
 * Implementing classes can choose how to format
 * the response, which will be returned by the
 * implementing class's toString() method.
 * Each implementing class must, however, provide
 * the following fields in each response:
 *   error - The error code returned by the endpoint.
 *     This is a simple string constant that can be
 *     matched against to determine the cause of an error.
 *     This will be set to the empty string if no error.
 *   message - The detailed (human-readable) message
 *     returned by the service.  If no error
 *     occurred (ie. error is the empty string), this
 *     field may be omitted.
 */
public interface Response {
	public String getError();
	public String getMessage();
}
