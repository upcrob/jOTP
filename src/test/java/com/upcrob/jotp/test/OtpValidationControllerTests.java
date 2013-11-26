package com.upcrob.jotp.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.Model;
import com.upcrob.jotp.controllers.OtpValidationController;

/**
 * Tests for the OtpValidationController
 */
public class OtpValidationControllerTests {
	@Test
	public void testNoUid() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("uid")).thenReturn(null);
		when(req.getParameter("token")).thenReturn("TOKEN");
		
		Model model = mock(Model.class);
		OtpValidationController ctl = new OtpValidationController(model);
		String ret = ctl.execute(req);
		assertEquals(ret, "{\"error\": \"No user identifier (uid) specified.\"}");
	}
	
	@Test
	public void testNoToken() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("uid")).thenReturn("realuser");
		when(req.getParameter("token")).thenReturn(null);
		
		Model model = mock(Model.class);
		OtpValidationController ctl = new OtpValidationController(model);
		String ret = ctl.execute(req);
		assertEquals(ret, "{\"error\": \"No token specified.\"}");
	}
	
	@Test
	public void testValidUser() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("uid")).thenReturn("realuser");
		when(req.getParameter("token")).thenReturn("TOKEN");
		
		Model model = mock(Model.class);
		when(model.isTokenValid("realuser", "TOKEN")).thenReturn(true);
		
		OtpValidationController ctl = new OtpValidationController(model);
		String ret = ctl.execute(req);
		assertEquals(ret, "{\"error\": \"\", \"tokenValid\": \"true\"}");
	}
	
	@Test
	public void testInvalidUser() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getParameter("uid")).thenReturn("realuser");
		when(req.getParameter("token")).thenReturn("TOKEN");
		
		Model model = mock(Model.class);
		when(model.isTokenValid("realuser", "TOKEN")).thenReturn(false);
		
		OtpValidationController ctl = new OtpValidationController(model);
		String ret = ctl.execute(req);
		assertEquals(ret, "{\"error\": \"\", \"tokenValid\": \"false\"}");
	}
}
