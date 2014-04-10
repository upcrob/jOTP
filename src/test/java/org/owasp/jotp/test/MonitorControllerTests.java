package org.owasp.jotp.test;

import java.util.HashMap;

import org.junit.Test;

import org.owasp.jotp.JsonResponse;
import org.owasp.jotp.controllers.MonitorController;

import static org.junit.Assert.*;

/**
 * Tests for the MonitorController.
 */
public class MonitorControllerTests {
	@Test
	public void testMonitorResponse() {
		MonitorController mc = new MonitorController();
		JsonResponse resp = new JsonResponse();
		resp.setField("status", "OK");
		assertEquals(mc.execute(new HashMap<String, String>()), resp);
	}
}
