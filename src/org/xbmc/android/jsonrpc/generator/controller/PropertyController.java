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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Produces a {@link Klass} or {@link Enum} for a given {@link Property}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class PropertyController {
	
	private final static Set<String> IGNORED_TYPES = new HashSet<String>();
	private final static Map<String, String> REPLACED_TYPES = new HashMap<String, String>();
	
	static {
		IGNORED_TYPES.add("Array.Integer");
		IGNORED_TYPES.add("Array.String");
		
		REPLACED_TYPES.put("Item.Fields.Base", "List<String>");
		REPLACED_TYPES.put("Optional.Integer", "Integer");
		REPLACED_TYPES.put("Optional.Number", "Double");
		REPLACED_TYPES.put("Optional.String", "String");
	}

	/**
	 * Name of the property.<p/>
	 * 
	 * Since most of the time, the name comes from the key of its parent, it's
	 * not saved in the property itself (exception: {@link Param}). In the 
	 * latter case, the name is read from the Param.
	 * <br>
	 * The only case where name is null is when it comes from a
	 * {@link Property#item} where there is no name whatsoever.
	 */
	private final String name;
	private final Property property;
	
	public PropertyController(String name, Property property) {
		if (property == null) {
			throw new IllegalArgumentException("Property parameter must not be null.");
		}
		this.property = property;
		this.name = name != null ? name : (property instanceof Param ? ((Param)property).getName() : null);
	}
	
	/**
	 * Registers a global type or enum that is declared in the 
	 * model package.
	 */
	public void register(String packageName) {
		
		// return directly if ignored type.
		if (name != null && (IGNORED_TYPES.contains(name) || REPLACED_TYPES.containsKey(name))) {
			return;
		}
		
		final Namespace ns = Namespace.get(name, packageName);
		
		if (!(property instanceof Type)) {
			throw new IllegalArgumentException("Only global types can be registered.");
		}
		final Type type = (Type)property;
		
		// class/enum name
		final String strippedName = name.contains(".") ? name.substring(name.indexOf(".") + 1) : name;
		final Property p = type.obj();
		
		// either register class or enum
		if (p.isEnum()) {
			ns.addEnum(getEnum(strippedName));
			ns.addImport("java.util.HashSet");
			ns.addImport("java.util.Set");
			ns.addImport("java.util.Arrays");
			
		} else if (p.isArray() && p.getItems().isEnum()) {
			/* So if we have an array that is made out of enums, it's
			 * really a array of Strings. But still, we should define 
			 * the enums somewhere, so we ignore the type and treat
			 * the array type.
			 */
			final PropertyController pc = new PropertyController(name, type.getItems());
			ns.addEnum(pc.getEnum(strippedName));
			ns.addImport("java.util.HashSet");
			ns.addImport("java.util.Set");
			ns.addImport("java.util.Arrays");
		} else {
			ns.addClass(getClass(ns, strippedName));
		}
	}
	
	/**
	 * Creates the agnostic {@link Klass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public Klass getClass(Namespace namespace, String className) {
		
		final Klass klass;
		
		// some basic tests
		if (property.hasProperties()) {
			if (property.isNative() || property.isArray() || property.isMultitype() || property.isRef()) {
				throw new RuntimeException("doh!");
			}
		}
		
		// create class from native type
		if (property.isNative()) {
			klass = new Klass(namespace, property.getType().getName());
			klass.setNative(true);
			
		// create class from multiple values
		} else if (property.isMultitype()) {
			final List<Type> types = property.getType().getList();
			
			if (property instanceof Type && ((Type)property).getId() != null) {
				klass = new Klass(namespace, className, ((Type)property).getId());
			} else {
				klass = new Klass(namespace, className);
				klass.setInner(true);
			}
			klass.setMultiType(true);
			
			for (Type t : types) {
				final String multiTypeName = findName(t);
				final MemberController mc = new MemberController(multiTypeName, t);
				klass.addMember(mc.getMember(namespace));
				if (t.isObjectDefinition()) {
					final PropertyController pc = new PropertyController(multiTypeName, t);
					final Klass typeClass = pc.getClass(namespace, multiTypeName);
					if (!t.isRef()) {
						typeClass.setInner(true);
						klass.addInnerType(typeClass);
					}
				}
			}
			
		// create class from array	
		} else if (property.isArray()) {
			final PropertyController pc = new PropertyController(null, property.getItems());
			klass = new Klass(namespace);
			klass.setArray(true);
			final Klass arrayType = pc.getClass(namespace, className);
			if (!property.getItems().isRef()) {
				arrayType.setInner(true);
			}
			klass.setArrayType(arrayType);
			klass.addImport("java.util.List");
			
			// arrays can also be defined as globals (List.Items.Sources)
			if (property instanceof Type && ((Type)property).getId() != null) {
				klass.setInner(false);
				klass.setGlobal(true);
				klass.getArrayType().setInner(false);
				klass.getArrayType().setGlobal(true);
			}
			
		// create class from reference
		} else if (property.isRef()) {
			klass = new Klass(property.getRef());
			
		// create class from global type
		} else if (property instanceof Type) {
			final Type type = (Type)property;
			
			// TODO check why the fuck ID would be null.
			if (type.getId() == null) {
				klass = new Klass(namespace, className);
			} else {
				klass = new Klass(namespace, findName(((Type)property).getId()), name);
				klass.setGlobal(true); // TODO adopt accordingly, see above.
			}
		
		// create class from object	
		} else if (property.hasProperties() ) {
			klass = new Klass(namespace, className);
			klass.setInner(true);
			
		// confusion!
		} else {
			throw new RuntimeException("Unexpected property type. Put breakpoint and check code :p");
		}

		// parse properties
		if (property.hasProperties()) {
			for (String propertyName : property.getProperties().keySet()) {
				final Property prop = property.getProperties().get(propertyName);
				
				final MemberController mc = new MemberController(propertyName, prop);
				final Member member = mc.getMember(namespace);
				
				// if type is inner or enum, reference here so it can properly rendered
				if (member.isInner()) {
					klass.addInnerType(member.getType());
				}
				if (member.isEnum()) {
					klass.addInnerEnum(member.getEnum());
				}
				
				if (member.isArray() && !member.getType().getArrayType().isNative() && !member.getType().getArrayType().isGlobal()) {
					klass.addInnerType(member.getType().getArrayType());
				}
				
				klass.addMember(member);
			}			
		}
		
		// create constructor(s)
		if (!klass.isUnresolved()) { 
			final ConstructorController cc = new ConstructorController(klass);
			for (Constructor c : cc.getConstructors()) {
				klass.addConstructor(c);
			}
		}
		
		return klass;
	}
	
	public String findName(String typeId) {
		String name = typeId;
		if (name.contains(".")) {
			name = name.substring(name.indexOf("."));
		}
		name = name.replace(".", "");
		
		return name;
	}
	
	public String findName(Property t) {
		
		if (!t.hasProperties() && !t.isNative() && !t.isRef() && !t.isArray()) {
			throw new RuntimeException("Cannot construct class name from multitype class without props!");
		}
		
		// native type
		if (t.isNative()) {
			return t.getType().getName() + "Arg";
		}
		
		// global type
		if (t.isRef()) {
			String name = findName(t.getRef());
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
		
		// array
		if (t.isArray()) {
			return findName(t.getItems()) + "List";
		}
		
		// object containing props
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String name : t.getProperties().keySet()) {
			if (i == 0) {
				// first name lower case
				sb.append(name);
			} else {
				// then first letter upper case
				sb.append(name.substring(0, 1).toUpperCase());
				sb.append(name.substring(1));
			}
			i++;
		}
		return sb.toString();
	}
	
	/**
	 * Creates the agnostic {@link Enum} object.
	 * 
	 * @param enumName Name of the enum (retrieved from parent key)
	 * @return Enum object
	 */
	public Enum getEnum(String enumName) {
		final Enum e = new Enum(enumName, name);
		for (String enumValue : property.getEnums()) {
			e.addValue(enumValue);
		}
		return e;
	}
}
