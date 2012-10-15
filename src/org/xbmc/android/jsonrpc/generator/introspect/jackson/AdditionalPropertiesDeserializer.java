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
package org.xbmc.android.jsonrpc.generator.introspect.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

/**
 * Deserializes the <tt>additionalProperties</tt> value into 
 * {@link AdditionalPropertiesWrapper}.
 * <p/>
 * 
 * The reason for that is that <tt>additionalProperties</tt> can be multiple
 * types, which doesn't work well (like, not at all) with Java.
 *  
 * @author freezy <freezy@xbmc.org>
 */
public class AdditionalPropertiesDeserializer extends Deserializer<AdditionalPropertiesWrapper> {
	
	@Override
	public AdditionalPropertiesWrapper deserialize(JsonParser jsonParser, DeserializationContext c) throws IOException, JsonProcessingException {
		final ObjectCodec oc = jsonParser.getCodec();
		final JsonNode node = oc.readTree(jsonParser);
		if (node.isBoolean()) {
			return new AdditionalPropertiesWrapper(node.getBooleanValue());
		} else {
			return new AdditionalPropertiesWrapper(Introspect.OBJECT_MAPPER.readValue(node, TypeWrapper.class));
		}
	}
}
