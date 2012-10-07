package org.xbmc.android.jsonrpc.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

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
