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

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.xbmc.android.jsonrpc.generator.controller.MethodController;
import org.xbmc.android.jsonrpc.generator.controller.PropertyController;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Response;
import org.xbmc.android.jsonrpc.generator.introspect.Result;
import org.xbmc.android.jsonrpc.generator.introspect.jackson.AdditionalPropertiesDeserializer;
import org.xbmc.android.jsonrpc.generator.introspect.jackson.ExtendsDeserializer;
import org.xbmc.android.jsonrpc.generator.introspect.jackson.TypeDeserializer;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.AdditionalPropertiesWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.NamespaceView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;
import org.xbmc.android.jsonrpc.generator.view.module.classmodule.*;
import org.xbmc.android.jsonrpc.generator.view.module.parentmodule.ClassParentModule;
import org.xbmc.android.jsonrpc.generator.view.module.parentmodule.MethodParentModule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Main program. To make this work, update:
 * 
 * <ul><li>{@link #OUTPUT_FOLDER} where you want the java files placed (your source folder)</li>
 *      <li>{@link #MODEL_PACKAGE} in which package you want your model files</li>
 * </ul>
 * 
 * Folders will be created. Program will crash if no write permissions.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Introspect {
	
	public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	// date: git show -s --format="%ci" 99fa6fb
	public final static String XBMC_VERSION_HASH = "7f49891";
	public final static String XBMC_VERSION_DATE = "2012-12-09 07:19:22 -0800";
	public final static String XBMC_VERSION_BRANCH = "Branch.MASTER";
	public final static String XBMC_VERSION_TYPE = "Type.NIGHTLY";
	
	private static Result RESULT;
	
	private final static String MODEL_PACKAGE = "org.xbmc.android.jsonrpc.api.model";
	private final static String CALL_PACKAGE = "org.xbmc.android.jsonrpc.api.call";
	
	private final static String MODEL_CLASS_SUFFIX = "Model";
	private final static String CALL_CLASS_SUFFIX  = "";
	
	private final static String OUTPUT_FOLDER = "D:/dev/xbmc-jsonrpclib-android";
//	private final static String OUTPUT_FOLDER = "/home/tthomas/git/xbmc-jsonrpclib-android/";

	private final static List<String> IGNORED_METHODS = new ArrayList<String>();
	
	static {
		final SimpleModule module = new SimpleModule("xbmc-json-rpc", Version.unknownVersion());
		module.addDeserializer(TypeWrapper.class, new TypeDeserializer());
		module.addDeserializer(ExtendsWrapper.class, new ExtendsDeserializer());
		module.addDeserializer(AdditionalPropertiesWrapper.class, new AdditionalPropertiesDeserializer());
		
		OBJECT_MAPPER.registerModule(module);
		IGNORED_METHODS.add("JSONRPC.Introspect"); // don't care. also, there is no return type definition.
		IGNORED_METHODS.add("XBMC.GetInfoBooleans"); // temporarily until fixed
		IGNORED_METHODS.add("XBMC.GetInfoLabels");   // temporarily until fixed
	}
	
	/**
	 * Main program
	 * @param args none
	 */
	public static void main(String[] args) {
		
		final long started = System.currentTimeMillis();
		
		try {
			
			// parse from json
			final Response response = OBJECT_MAPPER.readValue(new File("introspect.json"), Response.class);
			RESULT = response.getResult();

			final IClassModule[] typeClassModules = { 
				new MemberDeclarationClassModule(), 
				new JsonAccesClassModule(), 
				new ModelParcelableClassModule(),
				new ConvenienceExtensionsClassModule()
			};

			// register types
			final SortedSet<String> typeNames = new TreeSet<String>(RESULT.getTypes().keySet());
			for (String name : typeNames) {
				final PropertyController controller = new PropertyController(name, RESULT.getTypes().get(name));
				final Namespace ns = controller.register(MODEL_PACKAGE, MODEL_CLASS_SUFFIX);
				ns.addClassModule(typeClassModules);
				ns.addInnerClassModule(typeClassModules);
				ns.setParentModule(new ClassParentModule());
				ns.setInnerParentModule(new ClassParentModule());
			}
			
			// resolve types
			for (Namespace ns : Namespace.getTypes()) {
				ns.resolveChildren();
			}

			// register methods
			final SortedSet<String> methodNames = new TreeSet<String>(RESULT.getMethods().keySet());
			for (String name : methodNames) {
				if (!IGNORED_METHODS.contains(name)) {
					final MethodController controller = new MethodController(name, RESULT.getMethods().get(name));
					final Namespace ns = controller.register(CALL_PACKAGE, CALL_CLASS_SUFFIX);
					ns.addClassModule(new MethodAPIClassModule(), new CallParcelableClassModule());
					ns.addInnerClassModule(typeClassModules);
					ns.setParentModule(new MethodParentModule());
					ns.setInnerParentModule(new ClassParentModule());
				}
			}

			// resolve methods
			for (Namespace ns : Namespace.getMethods()) {
				ns.resolveChildren();
			}

			// pre-process imports
			for (Namespace ns : Namespace.getAll()) {
				ns.findModuleImports();
			}

			// 1. copy static classes
			final String relRoot = "org/xbmc/android/jsonrpc";
			final File destRoot = new File(OUTPUT_FOLDER + "/src/" + relRoot);
			if (!destRoot.exists()) {
				if (!destRoot.mkdirs()) {
					throw new RuntimeException("Cannot create folder " + destRoot.getAbsolutePath() + ".");
				}
			}
			FileUtils.copyDirectory(new File("tpl/" + relRoot), destRoot);

			// 2. render
			for (Namespace ns : Namespace.getAll()) {
				render(ns);
			}

			// 3. copy resources
			final File resRoot = new File(OUTPUT_FOLDER + "/res/");
			if (!resRoot.exists()) {
				if (!resRoot.mkdir()) {
					throw new RuntimeException("Cannot create folder " + resRoot.getAbsolutePath() + ".");
				}
			}
			FileUtils.copyDirectory(new File("res/"), resRoot);

			// 4. while we're at it, copy necessary libs
			final File libRoot = new File(OUTPUT_FOLDER + "/libs/");
			if (!libRoot.exists()) {
				if (!libRoot.mkdir()) {
					throw new RuntimeException("Cannot create folder " + libRoot.getAbsolutePath() + ".");
				}
			}
			FileUtils.copyFile(new File("libs/jackson-core-asl-1.8.8.jar"), new File(OUTPUT_FOLDER + "/libs/jackson-core-asl-1.8.8.jar"));
			FileUtils.copyFile(new File("libs/jackson-mapper-asl-1.8.8.jar"), new File(OUTPUT_FOLDER + "/libs/jackson-mapper-asl-1.8.8.jar"));
			
			// 5. update version file
			final File versionFile = new File(OUTPUT_FOLDER + "/src/org/xbmc/android/jsonrpc/api/Version.java");
			replaceInFile("%hash%", XBMC_VERSION_HASH, versionFile);
			replaceInFile("%date%", XBMC_VERSION_DATE, versionFile);
			replaceInFile("Branch.UNKNOWN", XBMC_VERSION_BRANCH, versionFile);
			replaceInFile("Type.UNKNOWN", XBMC_VERSION_TYPE, versionFile);

			System.out.println("Done in " + (System.currentTimeMillis() - started) + "ms.");

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void render(Namespace ns) {
		
		// do nothing if no classes or enums to render.
		if (ns.isEmpty()) {
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		final NamespaceView view = new NamespaceView(ns);
		final File out = getFile(ns);
		view.render(sb);
		if (sb.length() > 0) {
			writeFile(out, sb.toString());
		}
	}

	/**
	 * Computes the filename of the Java class file based on 
	 * {@link Introspect#OUTPUT_FOLDER} and the package of the namespace.
	 * 
	 * @param ns Namespace
	 * @return File handler
	 */
	private static File getFile(Namespace ns) {
		final StringBuilder sb = new StringBuilder(OUTPUT_FOLDER.replace("\\", "/"));
		final String[] packages = ns.getPackageName().split("\\.");
		if (!sb.toString().endsWith("/")) {
			sb.append("/");
		}
		sb.append("src/");
        for (String pak : packages) {
            sb.append(pak);
            sb.append("/");
        }
		sb.append(ns.getName());
		sb.append(".java");
		
		return new File(sb.toString());
	}
	
	/**
	 * Creates folder structure and dumps contents into file.
	 * @param file File to write to
	 * @param contents Data to dump
	 */
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
	
	private static void replaceInFile(String oldString, String newString, File f) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(f));
		final StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line.replaceAll(oldString, newString));
			sb.append("\r\n");
		}
		reader.close();
		
		final PrintWriter writer = new PrintWriter(new FileWriter(f));
		writer.print(sb.toString());
		writer.close();
	}
	
	public static Property find(String name) {
		if (RESULT == null) {
			throw new RuntimeException("Must parse before finding types!");
		}
		if (!RESULT.getTypes().containsKey(name)) {
			throw new RuntimeException("Cannot find type " + name + ".");
		}
		return RESULT.getTypes().get(name);
	}
	
	public static Property find(Property property) {
		if (property.isRef()) {
			return find(property.getRef());
		} else {
			return property;
		}
	}
}
