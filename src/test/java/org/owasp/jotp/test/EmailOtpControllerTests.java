package org.owasp.jotp.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.owasp.jotp.Client;
import org.owasp.jotp.Configuration;
import org.owasp.jotp.Response;
import org.owasp.jotp.Sender;
import org.owasp.jotp.SenderException;
import org.owasp.jotp.Tokenstore;
import org.owasp.jotp.controllers.EmailOtpController;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the EmailOtpController.
 */
public class EmailOtpControllerTests {
	
	private Map<String, Client> dummyGroups;
	
	@Before
	public void setup() {
		dummyGroups = new HashMap<String, Client>();
		Client a = new Client();
		Client b = new Client();
		b.setPassword("bpass");
		dummyGroups.put("a", a);
		dummyGroups.put("b", b);
	}
	
	@Test
	public void testNoGroup() {
		Map<String, String> params = new HashMap<String, String>();
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testNoGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testWrongGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("clientpassword", "wrong");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testNoAddress() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "ADDR");
	}
	
	@Test
	public void testInvalidAddress() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("address", "notvalidaddr");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "ADDR");
	}
	
	@Test
	public void testValidAddress() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("address", "test-test.test_test@example.com");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		eoc.setSender(mock(Sender.class));
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "");
	}
	
	@Test
	public void testSendException() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("address", "test@example.com");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		EmailOtpController eoc = new EmailOtpController(config, tokenstore);
		Sender s = mock(Sender.class);
		eoc.setSender(s);
		
		try {
			doThrow(new SenderException("")).when(s).send(eq("test@example.com"), anyString());
		} catch (SenderException e) {
			// do nothing
		}
		
		Response ret = eoc.execute(params);
		assertEquals(ret.getError(), "SEND");
	}
}
