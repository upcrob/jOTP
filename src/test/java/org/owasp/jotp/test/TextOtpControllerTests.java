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
import org.owasp.jotp.controllers.TextOtpController;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the TextOtpController.
 */
public class TextOtpControllerTests {
	
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
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testInvalidGroup() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "invalidclient");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testInvalidGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("client", "invalidclientpass");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testNoGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "GROUP");
	}
	
	@Test
	public void testValidGroupPassword() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "b");
		params.put("clientpassword", "bpass");
		params.put("number", "5555555555");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		toc.setSender(mock(Sender.class));
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "");
	}
	
	@Test
	public void testNoPhone() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "NO_PHONE");
	}
	
	@Test
	public void testInvalidNumber() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("number", "notvalid");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		toc.setSender(mock(Sender.class));
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "NO_PHONE");
	}
	
	@Test
	public void testValidNumber() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("number", "5555555555");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		toc.setSender(mock(Sender.class));
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "");
	}
	
	@Test
	public void testValidNumber2() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("number", "555-555-5555");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController toc = new TextOtpController(config, tokenstore);
		toc.setSender(mock(Sender.class));
		Response ret = toc.execute(params);
		assertEquals(ret.getError(), "");
	}
	
	@Test
	public void testSendException() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client", "a");
		params.put("number", "5555555555");
		
		Configuration config = mock(Configuration.class);
		when(config.getClients()).thenReturn(dummyGroups);
		when(config.isBlockingSmtp()).thenReturn(true);
		Tokenstore tokenstore = mock(Tokenstore.class);
		TextOtpController c = new TextOtpController(config, tokenstore);
		Sender s = mock(Sender.class);
		c.setSender(s);
		
		try {
			doThrow(new SenderException("")).when(s).send(anySetOf(String.class), anyString());
		} catch (SenderException e) {
			// do nothing
		}
		
		Response ret = c.execute(params);
		assertEquals(ret.getError(), "SEND");
	}
}
