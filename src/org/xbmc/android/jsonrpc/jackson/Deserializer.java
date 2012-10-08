/*
 *      Copyright (C) 2005-2012 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */
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
