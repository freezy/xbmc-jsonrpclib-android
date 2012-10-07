package org.xbmc.android.jsonrpc.generator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Response;
import org.xbmc.android.jsonrpc.generator.introspect.Result;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.jackson.AdditionalPropertiesDeserializer;
import org.xbmc.android.jsonrpc.jackson.ExtendsDeserializer;
import org.xbmc.android.jsonrpc.jackson.TypeDeserializer;

public class Introspect {
	
	public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	static {
		final SimpleModule module = new SimpleModule("", Version.unknownVersion());
		module.addDeserializer(TypeWrapper.class, new TypeDeserializer());
		module.addDeserializer(ExtendsWrapper.class, new ExtendsDeserializer());
		module.addDeserializer(AdditionalPropertiesWrapper.class, new AdditionalPropertiesDeserializer());
		
		OBJECT_MAPPER.registerModule(module);
//		OBJECT_MAPPER.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
//		OBJECT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
	}
	
	public static void main(String[] args) {
		try {
			
		    //final Map<String,Object> userData = mapper.readValue(new File("R:/phplib/xbmc-introspect.json"), Map.class);
		    final Response response = OBJECT_MAPPER.readValue(new File("R:/phplib/xbmc-introspect.json"), Response.class);
		    final Result result = response.getResult();
			
			final List<Param> params = result.getMethods().get("Addons.ExecuteAddon").getParams();
			for (Param param : params) {
				System.out.println(param);
			}
			System.out.println("Done!");
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
