package com.upcrob.jotp.test;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Model;
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
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid email specified.\"}");
	}
	
	@Test
	public void testInvalidAddress() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn("notvalidaddr");
		
		Configuration config = mock(Configuration.class);
		Model model = mock(Model.class);
		EmailOtpController eoc = new EmailOtpController(config, model);
		String ret = eoc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid email specified.\"}");
	}
}
