package com.upcrob.jotp.test;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.Configuration;
import com.upcrob.jotp.Model;
import com.upcrob.jotp.controllers.TextOtpController;

import static org.junit.Assert.*;
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
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid phone number specified.\"}");
	}
	
	@Test
	public void testInvalidAddress() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("address")).thenReturn("notvalid");
		
		Configuration config = mock(Configuration.class);
		Model model = mock(Model.class);
		TextOtpController toc = new TextOtpController(config, model);
		String ret = toc.execute(req);
		assertEquals(ret, "{\"error\":\"No valid phone number specified.\"}");
	}
}
