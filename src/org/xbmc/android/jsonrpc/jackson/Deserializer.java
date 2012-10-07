package org.xbmc.android.jsonrpc.jackson;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonDeserializer;
import org.xbmc.android.jsonrpc.exception.InvalidStringNodeException;

public abstract class Deserializer<T> extends JsonDeserializer<T> {
	
	protected Boolean getBooleanValue(JsonNode node, String name) {
		return node.has(name) ? node.get(name).getBooleanValue() : null;
	}
	
	protected String getTextValue(JsonNode node, String name) {
		return node.has(name) ? node.get(name).getTextValue(): null;
	}
	
	protected String convertToTextValue(JsonNode node, String name) throws InvalidStringNodeException {
        if (node.has(name)) {
        	final JsonNode n = node.get(name);
        	if (n.isBoolean()) {
        		return n.getBooleanValue() ? "true" : "false";
        	} else if (n.isTextual()) {
        		return n.getTextValue();
        	} else if (n.isInt()) {
        		return String.valueOf(n.getIntValue());
        	} else {
        		throw new InvalidStringNodeException("Cannot convert node to String.");
        	}
        } else {
        	return null;
        }
	}
	
}
