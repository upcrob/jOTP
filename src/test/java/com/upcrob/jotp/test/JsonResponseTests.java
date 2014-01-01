package com.upcrob.jotp.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.upcrob.jotp.JsonResponse;

public class JsonResponseTests {
	
	private JsonParser p = new JsonParser();
	
	@Test
	public void testNoErrorFormat() {
		JsonResponse resp = new JsonResponse();
		
		JsonObject o = p.parse(resp.toString()).getAsJsonObject();
		assertEquals("", o.get("error").getAsString());
		assertNull(o.get("message"));
	}
	
	@Test
	public void testErrorFormat() {
		JsonResponse resp = new JsonResponse("TESTERR", "My Message");
		
		JsonObject o = p.parse(resp.toString()).getAsJsonObject();
		assertEquals("TESTERR", o.get("error").getAsString());
		assertEquals("My Message", o.get("message").getAsString());
	}
	
	@Test
	public void testFieldFormat() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("a", "apple");
		m.put("b", "banana");
		m.put("c", "cranberry");
		JsonResponse resp = new JsonResponse(m);
		
		JsonObject o = p.parse(resp.toString()).getAsJsonObject();
		assertEquals("", o.get("error").getAsString());
		assertEquals("apple", o.get("a").getAsString());
		assertEquals("banana", o.get("b").getAsString());
		assertEquals("cranberry", o.get("c").getAsString());
	}
}
