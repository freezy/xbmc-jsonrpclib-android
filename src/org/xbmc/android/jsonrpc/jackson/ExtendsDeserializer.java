package org.xbmc.android.jsonrpc.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;

public class ExtendsDeserializer extends Deserializer<ExtendsWrapper> {
	
	@Override
	public ExtendsWrapper deserialize(JsonParser jsonParser, DeserializationContext c) throws IOException, JsonProcessingException {
		final ObjectCodec oc = jsonParser.getCodec();
		final JsonNode node = oc.readTree(jsonParser);
		if (node.isArray()) {
			return new ExtendsWrapper(new ArrayList<String>(Arrays.asList(Introspect.OBJECT_MAPPER.readValue(node, String[].class))));
		} else {
			return new ExtendsWrapper(node.getTextValue());
		}
	}
}
