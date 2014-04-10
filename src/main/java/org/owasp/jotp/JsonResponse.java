package org.owasp.jotp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonResponse implements Response {

	private String error;
	private String message;
	private Map<String, String> fields;
	
	public JsonResponse() {
		error = "";
		message = "";
		fields = null;
	}
	
	public JsonResponse(String error, String message) {
		if (error == null || message == null)
			throw new IllegalArgumentException("Error and message must be non-null");
		this.error = error;
		this.message = message;
		this.fields = null;
	}
	
	public JsonResponse(Map<String, String> fields) {
		error = "";
		message = "";
		this.fields = fields;
	}
	
	public void setField(String key, String value) {
		if ("error".equals(key) || "message".equals(key))
			throw new IllegalArgumentException("Invalid key.");
		if (fields == null)
			fields = new HashMap<String, String>();
		fields.put(key, value);
	}
	
	@Override
	public String toString() {
		// Append error field
		StringBuilder sb = new StringBuilder();
		sb.append("{\"error\":\"");
		sb.append(error.replace("\"", "\\\""));
		sb.append("\"");
		
		// Append message field if an error was present
		if (!error.equals("")) {
			sb.append(", \"message\":\"");
			sb.append(message.replace("\"", "\\\""));
			sb.append("\"");
		}
		
		// Append fields
		if (fields != null) {
			Set<Entry<String, String>> entries = fields.entrySet();
			for (Entry<String, String> entry : entries) {
				sb.append(", \"");
				sb.append(entry.getKey().replace("\"", "\\\""));
				sb.append("\":\"");
				sb.append(entry.getValue().replace("\"", "\\\""));
				sb.append("\"");
			}
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String getError() {
		return error;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JsonResponse))
			return false;
		JsonResponse r = (JsonResponse) o;
		if (!error.equals(r.error))
			return false;
		if (fields.size() != r.fields.size())
			return false;
		
		Set<Entry<String, String>> rset = r.fields.entrySet();
		for (Entry<String, String> field : fields.entrySet()) {
			if (!rset.contains(field))
				return false;
		}
		return true;
	}
}
