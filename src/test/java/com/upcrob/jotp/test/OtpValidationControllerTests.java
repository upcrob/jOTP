package com.upcrob.jotp.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.upcrob.jotp.Client;
import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.JsonResponse;
import com.upcrob.jotp.Tokenstore;
import com.upcrob.jotp.Response;
import com.upcrob.jotp.TokenstoreException;
import com.upcrob.jotp.controllers.OtpValidationController;

/**
 * Tests for the OtpValidationController
 */
public class OtpValidationControllerTests {
	
	private Configuration config;
	
	@Before
	public void setup() {
		Map<String, Client> dummyGroups = new HashMap<String, Client>();
		Client a = new Client();
		Client b = new Client();
		b.setPassword("bpass");
		dummyGroups.put("a", a);
		dummyGroups.put("b", b);
		
		config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
	}
	
	@Test
	public void testNoGroup() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testInvalidGroup() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "c");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testNoGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testWrongGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("clientPassword", "badpass");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testCorrectGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("clientpassword", "bpass");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertNotSame(ret.getError(), "GROUP");
	}
	
	@Test
	public void testNoUid() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "NO_UID");
	}
	
	@Test
	public void testNoToken() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("uid", "realuser");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		Response ret = ctl.execute(params);
		assertEquals(ret.getError(), "NO_TOKEN");
	}
	
	@Test
	public void testValidUser() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("uid", "realuser");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		try {
			when(tokenstore.isTokenValid("a", "realuser", "TOKEN")).thenReturn(true);
		} catch (TokenstoreException e) {
			fail();
		}
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		
		JsonResponse cmp = new JsonResponse();
		cmp.setField("tokenValid", "true");
		Response ret = ctl.execute(params);
		assertEquals(ret, cmp);
	}
	
	@Test
	public void testInvalidUser() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("uid", "realuser");
		params.put("token", "TOKEN");
		
		Tokenstore tokenstore = mock(Tokenstore.class);
		try {
			when(tokenstore.isTokenValid("a", "realuser", "TOKEN")).thenReturn(false);
		} catch (TokenstoreException e) {
			fail();
		}
		
		OtpValidationController ctl = new OtpValidationController(config, tokenstore);
		
		JsonResponse cmp = new JsonResponse();
		cmp.setField("tokenValid", "false");
		Response ret = ctl.execute(params);
		assertEquals(ret, cmp);
	}
}
