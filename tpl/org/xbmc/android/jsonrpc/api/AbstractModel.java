/*
 *      Copyright (C) 2005-2015 Team XBMC
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

package org.xbmc.android.jsonrpc.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONException;

import android.os.Parcelable;

public abstract class AbstractModel implements JsonSerializable, Parcelable {
	
	/**
	 * Reference to Jackson's object mapper
	 */
	protected final static ObjectMapper OM = new ObjectMapper();
	
	protected String mType;
	
	/**
	 * Tries to read an integer from JSON object.
	 * 
	 * @param node JSON object
	 * @param key Key
	 * @return Integer value if found, -1 otherwise.
	 * @throws JSONException
	 */
	public static int parseInt(JsonNode node, String key) {
		return node.has(key) ? node.get(key).getIntValue() : -1;
	}
	
	/**
	 * Tries to read an integer from JSON object.
	 * 
	 * @param node JSON object
	 * @param key Key
	 * @return String value if found, null otherwise.
	 * @throws JSONException
	 */
	public static String parseString(JsonNode node, String key) {
		return node.has(key) ? node.get(key).getTextValue() : null;
	}
	
	/**
	 * Tries to read an boolean from JSON object.
	 * 
	 * @param node JSON object
	 * @param key Key
	 * @return String value if found, null otherwise.
	 * @throws JSONException
	 */
	public static Boolean parseBoolean(JsonNode node, String key) {
		final boolean hasKey = node.has(key);
		if (hasKey) {
			return node.get(key).getBooleanValue();
		} else {
			return null;
		}
	}
	
	public static Double parseDouble(JsonNode node, String key) {
		return node.has(key) ? node.get(key).getDoubleValue() : null;
	}

	public static ArrayList<String> getStringArray(JsonNode node, String key) {
		if (node.has(key)) {
			final ArrayNode a = (ArrayNode)node.get(key);
			final ArrayList<String> l = new ArrayList<String>(a.size());
			for (int i = 0; i < a.size(); i++) {
				l.add(a.get(i).getTextValue());
			}
			return l;
		}
		return new ArrayList<String>(0);
	}
	
	public static ArrayList<Integer> getIntegerArray(JsonNode node, String key) {
		if (node.has(key)) {
			final ArrayNode a = (ArrayNode)node.get(key);
			final ArrayList<Integer> l = new ArrayList<Integer>(a.size());
			for (int i = 0; i < a.size(); i++) {
				l.add(a.get(i).getIntValue());
			}
			return l;
		}
		return new ArrayList<Integer>(0);
	}
	
	public static HashMap<String, String> getStringMap(JsonNode node, String key) {
		if (node.has(key)) {
			final ObjectNode n = (ObjectNode)node.get(key);
			final HashMap<String, String> m = new HashMap<String, String>();
			final Iterator<String> it = n.getFieldNames();
			while (it.hasNext()) {
				final String fieldName = it.next();
				m.put(fieldName, n.get(fieldName).getValueAsText());
			}
		}
		return new HashMap<String, String>();
	}

	
}
