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
import java.util.Map;

import org.xbmc.android.jsonrpc.generator.introspect.Method;
import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.JavaParameter;
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
	private final Map<String, Integer> innerClassDupes = new HashMap<String, Integer>(); 
	
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
		
		// 1. parameters
		for (Param p : method.getParams()) {
			
			if (p.isEnum()) {
				// TODO
			} else if (p.isArray() && p.getItems().isEnum()) {
				// TODO
			} else {
				
				final TypeWrapper tr = p.getType();
				
				// single type
				if (!p.isMultitype() || Helper.equalNativeTypes(tr.getList())) {
					for (JavaConstructor jc : constructors) {
						jc.addParameter(getParam(p.getName(), p, namespace, klass));
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
							for (JavaConstructor jc : constructors) {
								jc.addParameter(getParam(p.getName(), t, namespace, klass));
							}
						} else {
							// second..nth multitype: for each previously saved constructor, copy then add param
							for (JavaConstructor jc : copiedConstructors) {
								final JavaConstructor jjc = jc.copy();
								jjc.addParameter(getParam(p.getName(), t, namespace, klass));
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
	private JavaParameter getParam(String name, Property p, Namespace namespace, JavaClass klass) {
		final PropertyController pc = new PropertyController(name, p);
		final JavaClass type = pc.getClass(namespace, name, klass);
		final JavaParameter jp = new JavaParameter(name, type);
		jp.setDescription(p.getDescription());
		if (type.isInner()) {
			final String k = type.getName();
			if (innerClassDupes.containsKey(k)) {
				int suffix = innerClassDupes.get(k) + 1;
				type.suffixName(String.valueOf(suffix));
				innerClassDupes.put(k, suffix);
			} else {
				innerClassDupes.put(k, 1);
			}
			klass.linkInnerType(type);
			
		}
		return jp;
	}

}
