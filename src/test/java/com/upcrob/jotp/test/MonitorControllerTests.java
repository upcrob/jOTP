package com.upcrob.jotp.test;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.upcrob.jotp.controllers.MonitorController;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the MonitorController.
 */
public class MonitorControllerTests {
	@Test
	public void testMonitorResponse() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		MonitorController mc = new MonitorController();
		assertEquals(mc.execute(req), "{\"status\": \"OK\"}");
	}
}
