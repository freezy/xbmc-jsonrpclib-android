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
package org.xbmc.android.jsonrpc.generator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xbmc.android.jsonrpc.generator.introspect.Method;
import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.generator.model.JavaAttribute;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaEnum;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Produces a {@link JavaClass} for a given {@link Method}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MethodController {

	private final String name;
	private final String apiType;
	private final Method method;
	private final Map<String, JavaClass> innerClassDupes = new HashMap<String, JavaClass>(); 
	
	private final static String RESULT_CLASS_SUFFIX = "Result";
	private final static List<String> META_RETURN_PROPS = new ArrayList<String>();
	
	static {
		META_RETURN_PROPS.add("limits");
	}
	
	/**
	 * Creates a new method controller.
	 * @param apiType Full name of the method, e.g.: <tt>Addons.ExecuteAddon</tt>
	 * @param method Unserialized JSON object
	 */
	public MethodController(String apiType, Method method) {
		if (method == null) {
			throw new IllegalArgumentException("Method parameter must not be null.");
		}
		this.method = method;
		this.apiType = apiType;
		this.name = apiType.contains(".") ? apiType.substring(apiType.indexOf(".") + 1) : apiType;
	}
	
	/**
	 * Registers a method.
	 * @param packageName 
	 */
	public Namespace register(String packageName, String classSuffix) {
		
		final Namespace ns = Namespace.getMethod(apiType, packageName, classSuffix);
		ns.addClass(getClass(ns, name));
		
		return ns;
	}
	
	/**
	 * Creates the agnostic {@link JavaClass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public JavaMethod getClass(Namespace namespace, String methodName) {
		
		final JavaMethod klass = new JavaMethod(namespace, name, apiType);
		
		final List<JavaConstructor> constructors = new ArrayList<JavaConstructor>();
		constructors.add(new JavaConstructor(klass));
		JavaAttribute properties = null;
		
		// 1. parameters
		for (Param p : method.getParams()) {

			if (p.isEnum()) {
				final JavaAttribute jp = getParam(p.getName(), p, namespace, klass);
				// add parameter to all constructor
				for (JavaConstructor jc : constructors) {
					jc.addParameter(jp);
				}
			} else if (p.isArray() && p.getItems().isEnum()) {
				throw new UnsupportedOperationException("No support for params that are arrays of enums yet.");
				
			} else {
				
				final TypeWrapper tr = p.getType();
				
				// single type
				if (!p.isMultitype() || Helper.equalNativeTypes(tr.getList())) {
					final JavaAttribute jp = getParam(p.getName(), p, namespace, klass);
					
					// "properties" is a special case, we add them at the end.
					if (p.getName().equals("properties")) {
						properties = jp;
						continue;
					}
					
					// add parameter to all constructor
					for (JavaConstructor jc : constructors) {
						jc.addParameter(jp);
					}
					
				// multitype
				} else {
					
					// copy current constructors so we can copy them for each additional type
					final List<JavaConstructor> copiedConstructors = new ArrayList<JavaConstructor>();
					for (JavaConstructor jc : constructors) {
						copiedConstructors.add(jc.copy());
					}
					
					int i = 0;
					for (Type t : tr.getList()) {
						if (i == 0) {
							// first multitype: just add param to current constructors.
							final JavaAttribute jp = getParam(p.getName(), t, namespace, klass);
							jp.setRequired(p.isRequired());
							for (JavaConstructor jc : constructors) {
								jc.addParameter(jp);
							}
						} else {
							// second..nth multitype: for each previously saved constructor, copy then add param
							final JavaAttribute jp = getParam(p.getName(), t, namespace, klass);
							jp.setRequired(p.isRequired());
							for (JavaConstructor jc : copiedConstructors) {
								final JavaConstructor jjc = jc.copy();
								jjc.addParameter(jp);
								boolean dupeFound = false;
								for (JavaConstructor jjjc : constructors) {
									if (jjjc.hasSameParams(jjc)) {
										dupeFound = true;
									}
								}
								if (!dupeFound) {
									constructors.add(jjc);
								}
							}
						}
						i++;
					}
				}
			}
		}
		
		// create constructor aliases without non-required args
		final int size = constructors.size();
		for (int j = 0; j < size; j++) {
			final JavaConstructor jc = constructors.get(j);
			final int numParams = jc.getParameters().size();
			if (numParams > 5) {
				continue;
			}
			final long n = (int)Math.pow(2, numParams) - 2;
			// l is a bitmask and counting down means getting all combinations
			for (Long l = n; l >= 0; l--) {
				final JavaConstructor alias = new JavaConstructor(jc.getType());
				// retrieve params
				for (int i = 0; i < numParams; i++) {
					long b = ((l >> i) & 1); // bit at position i
					final JavaAttribute param = jc.getParameters().get(i);
					if (b == 1 || param.isRequired()) {
						alias.addParameter(param);
					}
				}
				// check signature already there
				boolean found = false;
				for (JavaConstructor jjc : constructors) {
					if (jjc.hasSameParams(alias)) {
						found = true;
					}
				}
				if (!found) {
					constructors.add(alias);
				}
			}
		}
		
		// add properties if previously skipped
		if (properties != null) {
			for (JavaConstructor jc : constructors) {
				jc.addParameter(properties);
			}
		}
		klass.setConstructors(constructors);

		
		// 2. return type
		final TypeWrapper tw = method.getReturns();
		if (tw.isObject()) {
			
			final Type type = tw.getObj();
			
			// result type is either native, array, a type reference...
			if (type.isNative() || type.isRef() || type.isArray()) {
				
				final String name = klass.getName() + RESULT_CLASS_SUFFIX;
				final PropertyController returnTypeController = new PropertyController(null, method.getReturns().getObj());
				final JavaClass returnType = returnTypeController.getClass(namespace, name, klass);
				
				if (returnType.isTypeArray()) {
					klass.linkInnerType(returnType.getArrayType());
				}
				
				klass.setReturnType(returnType);

			// ...or an object	
			} else if (type.isObjectDefinition()) {
				/* 
				 * In case of an object, there are two scenarios. Either the
				 * the object is a "meta" object, meaning it contains meta data
				 * such as the limits of the result. In this case there is
				 * one non-meta property where the data is stored. Example:
				 *  "returns" : {
				 *  	"properties" : {
				 *  		"limits" : { "$ref" : "List.LimitsReturned", "required" : true },
				 *  		"sources" : { "$ref" : "List.Items.Sources", "required" : true }
				 *  	},
				 *  	"type" : "object"
				 *  }
				 *  If there is more than one "non-meta" property, we assume
				 *  it's a full-fledged object definition which will result in
				 *  an inner class:
				 *   "returns" : {
				 *   	"properties" : {
				 *   		"details" : { "description" : "Tran...", "required" : true, "type" : "any" },
				 *   		"mode" : { "description" : "Dir...", "enums" : ["redirect", "direct"], "required" : true, "type" : "string" },
				 *   		"protocol" : { "enums" : ["http"], "required" : true, "type" : "string" }
				 *   	},
				 *   	"type" : "object"
				 *   }
				 */
				if (!type.hasProperties() && !type.hasAdditionalProperties()) {
					throw new IllegalStateException("Definition is object but no props defined. That's seriously weird.");
				}
				
				if (type.hasProperties()) {
					
					// go through props and compare and count.
					String potentialResultPropName = null; // the non-meta prop in case there is only one.
					int nonMetaProps = 0;
					for (String propName : type.getProperties().keySet()) {
						if (!META_RETURN_PROPS.contains(propName)) {
							potentialResultPropName = propName;
							nonMetaProps++;
						}
					}
					
					// first case described above: data is wrapped into a meta object.
					if (nonMetaProps == 1) {
						final Property prop = type.getProperties().get(potentialResultPropName);
						if (!prop.isRef() && !prop.isNative() && !prop.isArray()) {
							throw new IllegalStateException("Return type is expected to be either reference, native or array");
						}
						
						final PropertyController returnTypeController = new PropertyController(null, prop);
						klass.setReturnType(returnTypeController.getClass(namespace, null, klass));
						klass.setReturnProperty(potentialResultPropName);
					
					// second case: full object definition. we suffix the class name with RESULT_CLASS_SUFFIX	
					} else {
						final String name = klass.getName() + RESULT_CLASS_SUFFIX;
						final PropertyController returnTypeController = new PropertyController(null, type);
						final JavaClass returnType = returnTypeController.getClass(namespace, name, klass);
						klass.setReturnType(returnType);
						klass.getInnerTypes().add(returnType);
					}
				} else {
					throw new UnsupportedOperationException("Naked objects with only additional attributes are not yet supported.");
				}
				
			} else {
				throw new IllegalStateException("Result type is expected to be a reference, native or an object.");
			}
			
		} else {
			throw new RuntimeException("Expected return type is an object with properties.");
		}
		
		// description
		klass.setDescription(method.getDescription());
		
		return klass;
	}
	
	
	/**
	 * Returns a JavaParameter for a given property. 
	 * @param name
	 * @param p
	 * @param namespace
	 * @param klass
	 * @return
	 */
	private JavaAttribute getParam(String name, Property p, Namespace namespace, JavaClass klass) {
		final JavaAttribute jp;
		final PropertyController pc = new PropertyController(name, p);
		if (p.isEnum()) {
			final JavaEnum e = pc.getEnum(namespace, name);
			jp = new JavaAttribute(name, e, klass);
			klass.linkInnerEnum(e);
		} else {
			final JavaClass type = pc.getClass(namespace, name, klass);
			jp = new JavaAttribute(name, type, klass);
			
			type.setUsedAsParameter();
			if (type.isInner()) {
				final String k = type.getName();
				if (innerClassDupes.containsKey(k)) {
					if (innerClassDupes.get(k) != null) {
						// update "old" type with new name and set null
						innerClassDupes.get(k).suffixName(getSuffixFromMembers(innerClassDupes.get(k)));
						innerClassDupes.put(k, null);
					} 
					// update type with new name
					type.suffixName(getSuffixFromMembers(type));
				} else {
					innerClassDupes.put(k, type);
				}
				klass.linkInnerType(type);
			}
		}
		jp.setDescription(p.getDescription());
		jp.setRequired(p.isRequired());
		return jp;
	}
	
	/**
	 * Returns some nicely readable suffix, generated from member names.
	 * @param klass To be suffixed class
	 * @return Suffix
	 */
	private String getSuffixFromMembers(JavaClass klass) {
		final StringBuilder sb = new StringBuilder();
		for (JavaAttribute m : klass.getMembers()) {
			final String name = m.getName().replace("id", "Id");
			sb.append(name.substring(0, 1).toUpperCase(Locale.ENGLISH));
			sb.append(name.substring(1));
		}
		return sb.toString();
	}

}
