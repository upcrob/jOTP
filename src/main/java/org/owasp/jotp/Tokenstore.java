package org.owasp.jotp;

/**
 * Describes a datastructure for storing and validating one-time
 * password tokens.
 */
public interface Tokenstore {
	/**
	 * Adds a token to the datastructure.
	 * 
	 * @param group Isolated client map under which to add the token.
	 * @param uid Unique identifier that is associated with the user
	 *   for which this token is generated.
	 * @param token Token string to store.
	 * @throws TokenstoreException Thrown if an error occurs in the
	 *   underlying store.
	 */
	public void putToken(String client, String uid, String token) throws TokenstoreException;
	
	/**
	 * Determines if a token is valid.  That is, if it exists in the
	 * Tokenstore and hasn't expired yet.  If it is valid, the method
	 * will then return true and automatically expire the token.
	 * 
	 * @param group Client map.
	 * @param uid User ID.
	 * @param Token string.
	 * @throws TokenstoreException Thrown if an error occurs in the
	 *   underlying store.
	 */
	public boolean isTokenValid(String client, String uid, String token) throws TokenstoreException;
	
	/**
	 * Removes all expired tokens currently in the Tokenstore.  Because
	 * calling this method may require a resource lock, it should be
	 * called only periodically for cleanup purposes.
	 * @throws TokenstoreException Thrown if an error occurs in the
	 *   underlying store.
	 */
	public void removeExpired() throws TokenstoreException;
}
