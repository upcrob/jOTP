package com.upcrob.jotp.test;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Model;
import com.upcrob.jotp.Sender;
import com.upcrob.jotp.SenderException;
import com.upcrob.jotp.controllers.TextOtpController;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for the TextOtpController.
 */
public class TextOtpControllerTests {
	@Test
	public void testNoPhone() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("number")).thenReturn(null);
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid phone number specified.\"}");
	}
	
	@Test
	public void testInvalidNumber() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("number")).thenReturn("notvalid");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		toc.setSender(mock(Sender.class));
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid phone number specified.\"}");
	}
	
	@Test
	public void testValidNumber() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("number")).thenReturn("5555555555");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		toc.setSender(mock(Sender.class));
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error:\": \"\"}");
	}
	
	@Test
	public void testValidNumber2() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("number")).thenReturn("555-555-5555");
		
		Configuration config = mock(Configuration.class);
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		toc.setSender(mock(Sender.class));
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error:\": \"\"}");
	}
	
	@Test
	public void testSendException() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("number")).thenReturn("5555555555");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		TextOtpController c = new TextOtpController(config, model);
		Sender s = mock(Sender.class);
		c.setSender(s);
		
		try {
			doThrow(new SenderException("")).when(s).send(anySetOf(String.class), anyString());
		} catch (SenderException e) {
			// do nothing
		}
		
		String ret = c.execute(req);
		assertEquals(ret, "{\"error\": \"Could not send token.\"}");
	}
}
