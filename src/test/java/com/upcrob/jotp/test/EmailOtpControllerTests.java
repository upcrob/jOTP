package com.upcrob.jotp.test;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Model;
import com.upcrob.jotp.Sender;
import com.upcrob.jotp.SenderException;
import com.upcrob.jotp.controllers.EmailOtpController;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the EmailOtpController.
 */
public class EmailOtpControllerTests {
	@Test
	public void testNoAddress() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn(null);
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		eoc.setSender(mock(Sender.class));
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error\": \"No valid email specified.\"}");
	}
	
	@Test
	public void testInvalidAddress() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn("notvalidaddr");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		eoc.setSender(mock(Sender.class));
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error\": \"No valid email specified.\"}");
	}
	
	@Test
	public void testValidAddress() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn("test-test.test_test@example.com");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		eoc.setSender(mock(Sender.class));
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error:\": \"\"}");
	}
	
	@Test
	public void testSendException() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn("test@example.com");
		
		Configuration config = mock(Configuration.class);
		when(config.isOptimisticResponse()).thenReturn(false);
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		Sender s = mock(Sender.class);
		eoc.setSender(s);
		
		try {
			doThrow(new SenderException("")).when(s).send(eq("test@example.com"), anyString());
		} catch (SenderException e) {
			// do nothing
		}
		
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error\": \"Could not send token.\"}");
	}
}
