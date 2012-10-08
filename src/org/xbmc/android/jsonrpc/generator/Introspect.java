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
package org.xbmc.android.jsonrpc.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.xbmc.android.jsonrpc.generator.controller.PropertyController;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Response;
import org.xbmc.android.jsonrpc.generator.introspect.Result;
import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.NamespaceView;
import org.xbmc.android.jsonrpc.jackson.AdditionalPropertiesDeserializer;
import org.xbmc.android.jsonrpc.jackson.ExtendsDeserializer;
import org.xbmc.android.jsonrpc.jackson.TypeDeserializer;

public class Introspect {
	
	public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static Result RESULT;
	
	private final static String MODEL_PACKAGE = "org.xbmc.android.jsonrpc.api.model";
//	private final static String CALL_PACKAGE = "org.xbmc.android.jsonrpc.api.call";
	
	private final static String OUTPUT_FOLDER = "D:/dev/xbmc-jsonrpclib-android-test/src";
	
	static {
		final SimpleModule module = new SimpleModule("", Version.unknownVersion());
		module.addDeserializer(TypeWrapper.class, new TypeDeserializer());
		module.addDeserializer(ExtendsWrapper.class, new ExtendsDeserializer());
		module.addDeserializer(AdditionalPropertiesWrapper.class, new AdditionalPropertiesDeserializer());
		
		OBJECT_MAPPER.registerModule(module);
	}
	
	public static void main(String[] args) {
		try {
			
			// parse from json
		    final Response response = OBJECT_MAPPER.readValue(new File("introspect.json"), Response.class);
		    RESULT = response.getResult();
			
		    // register types
		    for (String name : RESULT.getTypes().keySet()) {
		    	final PropertyController controller = new PropertyController(name, RESULT.getTypes().get(name));
		    	controller.register(MODEL_PACKAGE);
		    }
		    
		    // render types
		    for (Namespace ns : Namespace.getAll()) {
		    	final NamespaceView view = new NamespaceView(ns);
		    	final String data = view.render();
		    	final File out = getFile(ns);
		    	if (!data.isEmpty()) {
		    		writeFile(out, data);
		    		System.out.print(data);
		    	}
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
	
	
	private static File getFile(Namespace ns) {
		final StringBuffer sb = new StringBuffer(OUTPUT_FOLDER.replace("\\", "/"));
		final String[] paks = ns.getPackageName().split("\\.");
		if (!sb.toString().endsWith("/")) {
			sb.append("/");
		}
		for (int i = 0; i < paks.length; i++) {
			sb.append(paks[i]);
			sb.append("/");
		}
		sb.append(ns.getName());
		sb.append(".java");
		
		return new File(sb.toString());
	}
	
	private static void writeFile(File file, String contents) {
		final File path = file.getParentFile();
		
		// create folders
		if (!path.exists()) {
			if (!path.mkdirs()) {
				throw new IllegalArgumentException("Path " + path.getAbsolutePath() + " doesn't exist and cannot be created.");
			}
		}
		
		// dump to disk
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(contents);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static Property find(Property property) {
		if (RESULT == null) {
			throw new RuntimeException("Must parse before finding types!");
		}
		if (property.isRef()) {
			final Type type = RESULT.getTypes().get(property.getRef());
			if (!RESULT.getTypes().containsKey(property.getRef())) {
				throw new RuntimeException("Cannot find type " + property.getRef() + ".");
			}
			return type;
		} else {
			return property;
		}
	}
}
