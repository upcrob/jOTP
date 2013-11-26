package com.upcrob.jotp.test;

import static org.junit.Assert.*;

import org.junit.*;

import com.upcrob.jotp.Model;

/**
 * Tests for the Model class.
 */
public class ModelTests {
	
	private Model model = Model.getInstance();
	
	@Test
	public void testPutToken() {
		model.putToken("user@example.com", "TTTT", 10);
		assertTrue(model.isTokenValid("user@example.com", "TTTT"));
	}
	
	@Test
	public void testTokenValidComparison() {
		model.putToken("user@example.com", "TTTT", 10);
		assertFalse(model.isTokenValid("user@example.com", "TTT"));
	}

	@Test
	public void testNonexistantTokenKey() {
		assertFalse(model.isTokenValid("nobody@example.com", "T"));
	}
	
	@Test
	public void testRemoveToken() {
		model.putToken("user@example.com", "TTTT", 10);
		model.removeToken("user@example.com");
		assertFalse(model.isTokenValid("user@example.com", "TTTT"));
	}
	
	@Test
	public void testRemoveExpired() {
		model.putToken("user@example.com", "TTTT", 1);
		model.putToken("user2@example.com", "TTTT", 10);
		try {
			Thread.sleep(2000);
			model.removeExpired();
			assertFalse(model.isTokenValid("user@example.com", "TTTT"));
			assertTrue(model.isTokenValid("user2@example.com", "TTTT"));
		} catch (InterruptedException e) {
			fail("Test failed due to thread interrupt.");
		}
	}
}
