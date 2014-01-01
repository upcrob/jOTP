package com.upcrob.jotp.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import com.upcrob.jotp.Client;
import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.LocalTokenstore;

import static org.mockito.Mockito.*;

/**
 * Tests for the Model class.
 */
public class LocalTokenstoreTests {
	
	private Configuration config;
	
	@Before
	public void setup() {
		config = mock(Configuration.class);
		Map<String, Client> clients = new HashMap<String, Client>();
		Client a = new Client();
		a.setTokenLifetime(1);
		clients.put("a", a);
		when(config.getClients()).thenReturn(clients);
	}
	
	@Test
	public void testValidToken() {
		LocalTokenstore ts = new LocalTokenstore(config);
		ts.putToken("a", "user", "TTT");
		assertTrue(ts.isTokenValid("a", "user", "TTT"));
	}
	
	@Test
	public void testInvalidToken() {
		LocalTokenstore ts = new LocalTokenstore(config);
		assertFalse(ts.isTokenValid("b", "user", "TT"));
	}
	
	@Test
	public void testAutomaticInvalidate() {
		LocalTokenstore ts = new LocalTokenstore(config);
		ts.putToken("a", "user", "TTT");
		assertTrue(ts.isTokenValid("a", "user", "TTT"));
		assertFalse(ts.isTokenValid("a", "user", "TTT"));
	}
	
	@Test
	public void testRemoveExpired() {
		LocalTokenstore ts = new LocalTokenstore(config);
		ts.putToken("a", "user", "TTT");
		
		try {
			// Wait for token to become invalid before
			// calling removeExpired()
			Thread.sleep(2000);
			ts.removeExpired();
			assertFalse(ts.isTokenValid("a", "user", "TTT"));
		} catch (InterruptedException e) {
			fail("Testing thread interrupted.");
		}
	}
	
	@Test
	public void testKeepNonExpired() {
		LocalTokenstore ts = new LocalTokenstore(config);
		ts.putToken("a", "user", "TTT");
		ts.removeExpired();
		assertTrue(ts.isTokenValid("a", "user", "TTT"));
	}
}
