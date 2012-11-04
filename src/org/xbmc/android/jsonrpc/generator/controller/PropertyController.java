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

import java.util.List;

import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.Method;
import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaEnum;
import org.xbmc.android.jsonrpc.generator.model.JavaMember;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Produces a {@link JavaClass} or {@link JavaEnum} for a given {@link Property}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class PropertyController {
	
	/**
	 * Name of the property.<p/>
	 * 
	 * Since most of the time, the name comes from the key of its parent, it's
	 * not saved in the property itself (exception: {@link Param}). In the 
	 * latter case, the name is read from the Param.
	 * <br>
	 * The only cases where name is null is when it comes from
	 * {@link Property#items} or {@link Method#returns} where there is no name
	 * whatsoever.
	 */
	private final String name;
	private final Property property;
	
	private final String apiType;
	
	public PropertyController(String name, Property property) {
		if (property == null) {
			throw new IllegalArgumentException("Property parameter must not be null.");
		}
		this.property = property;
		
		// apiType we need sometimes, so parse it here.
		if (property instanceof Type && ((Type)property).getId() != null) {
			this.apiType = ((Type)property).getId();
		} else {
			this.apiType = null;
		}
		
		// read name from property name if name not set
		if (name == null && property instanceof Param) {
			this.name = ((Param)property).getName();
		} else {
			this.name = name;
		}
	}
	
	/**
	 * Registers a global type or enum that is declared in the 
	 * model package.
	 */
	public Namespace register(String packageName, String classSuffix) {
		
		final Namespace ns = Namespace.getType(name, packageName, classSuffix);
		
		if (!(property instanceof Type)) {
			throw new IllegalArgumentException("Only global types can be registered.");
		}
		final Type type = (Type)property;
		
		// class/enum name
		final String strippedName = name.contains(".") ? name.substring(name.indexOf(".") + 1) : name;
		final Property p = type.obj();
		
		// either register class or enum
		if (p.isEnum()) {
			ns.addEnum(getEnum(ns, strippedName));
			
		} else if (p.isArray() && p.getItems().isEnum()) {
			/* So if we have an array that is made out of enums, it's
			 * really a array of Strings. But still, we should define 
			 * the enums somewhere, so we ignore the type and treat
			 * the array type.
			 */
			final PropertyController pc = new PropertyController(name, type.getItems());
			ns.addEnum(pc.getEnum(ns, strippedName).setArray());
			
		} else {
			ns.addClass(getClass(ns, strippedName, null));
		}
		return ns;
	}
	
	/**
	 * Creates the agnostic {@link JavaClass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public JavaClass getClass(Namespace namespace, String className, JavaClass outerType) {
		
		final JavaClass klass;
		
		// some basic tests
		if (property.hasProperties()) {
			if (property.isNative() || property.isArray() || property.isMultitype() || property.isRef()) {
				throw new RuntimeException("Property has properties but either array, native, multitype or ref. That's weird!");
			}
		}
		if (outerType == null && !isGlobal()) {
			throw new IllegalArgumentException("Outer type must be set for non-global classes.");
		}
/*		final Property obj = property.obj();
		if (obj.isEnum()) {
			throw new IllegalArgumentException("Property must not be an enum.");
		}
		if (obj.isArray() && obj.getItems().obj().isEnum()) {
			throw new IllegalArgumentException("Property must not be a list of enums.");
		}*/
		
		// create class from native type
		if (property.isNative()) {
			
			if (isGlobal()) { // new class
				klass = new JavaClass(namespace, property.getType().getName(), apiType);
			} else {
				klass = new JavaClass(namespace, property.getType().getName());
			}
			klass.setNative();
			
		// create class from multiple values
		} else if (property.isMultitype()) {
			
			if (isGlobal()) { // new class
				klass = new JavaClass(namespace, className, apiType);
			} else {
				klass = new JavaClass(namespace, className);
				klass.setInner(outerType);
			}
			klass.setMultiType();
			
			final List<Type> types = property.getType().getList();
			for (Type t : types) {
				final String multiTypeName = findName(t);
				final MemberController mc = new MemberController(multiTypeName, t);
				klass.addMember(mc.getMember(namespace, klass));
				if (t.isObjectDefinition()) {
					final PropertyController pc = new PropertyController(multiTypeName, t);
					final JavaClass typeClass = pc.getClass(namespace, multiTypeName, klass);
					if (!t.isRef()) {
						klass.linkInnerType(typeClass);
					}
				}
			}
			
		// create class from array	
		} else if (property.isArray()) {
			
			if (isGlobal()) { // new class
				klass = new JavaClass(namespace, null, apiType);
			} else {
				klass = new JavaClass(namespace);
			}
			
			// get array type
			final PropertyController pc = new PropertyController(null, property.getItems());
			klass.setArray(pc.getClass(namespace, className, klass));
			
			// arrays can also be defined as globals (List.Items.Sources)
			if (isGlobal()) {
				klass.setGlobal();
				klass.getArrayType().setGlobal();
			}
			
		// create class from reference (1)
		} else if (property.isRef()) {
			klass = new JavaClass(property.getRef());
			
		// create class from reference (2)
		} else if (property.getType() != null && property.getType().isObject() && property.getType().getObj().isRef()) {
			klass = new JavaClass(property.getType().getObj().getRef());
			
		// create class from global type
		} else if (property instanceof Type) {
			final Type type = (Type)property;
			
			// if id not set it's an inner type.
			if (type.getId() == null) {
				klass = new JavaClass(namespace, className);
				klass.setInner(outerType);
			} else {
				klass = new JavaClass(namespace, findName(apiType), name);
				klass.setGlobal();
			}
		
		// create class from (non-global) object	
		} else if (property.hasProperties() ) {
			klass = new JavaClass(namespace, className);
			klass.setInner(outerType);
			
		// wtf!
		} else {
			throw new IllegalStateException("Unexpected property type. Put breakpoint and check code :p");
		}

		// parse properties
		if (property.hasProperties()) {
			for (String propertyName : property.getProperties().keySet()) {
				final Property prop = property.getProperties().get(propertyName);
				
				final MemberController mc = new MemberController(propertyName, prop);
				final JavaMember member = mc.getMember(namespace, klass);
				
				// if type is inner or enum, reference here so it can properly rendered
				if (member.isInner()) {
					klass.linkInnerType(member.getType());
				}
				if (member.isEnum()) {
					klass.linkInnerEnum(member.getEnum());
				}
				
				if (member.isArray()) {
					final JavaClass arrayType = member.getType().getArrayType();
					if (!arrayType.isNative() && !arrayType.isGlobal()) {
						klass.linkInnerType(arrayType);
					}
				}
				
				// we must resolve the type if we want to find out if it's really an array.
				final Property propResolved = Introspect.find(prop);
				if (propResolved.isArray()) {
					klass.addImport("java.util.List");
				}
				
				klass.addMember(member);
			}			
		}
		
		// parse extends
		if (property.doesExtend()) {
			final ExtendsWrapper ew = property.getExtends();
			if (ew.isList()) {
				// TODO multiple heritage
			} else {
				klass.setParentClass(new JavaClass(property.getExtends().getName()));
			}
		}
		
		// description
		klass.setDescription(property.getDescription());
		
		// create constructor(s)
		if (!klass.isUnresolved()) { 
			final ConstructorController cc = new ConstructorController(klass);
			for (JavaConstructor c : cc.getConstructors()) {
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
	
	private boolean isGlobal() {
		return apiType != null;
	}
	
	/**
	 * Creates the agnostic {@link JavaEnum} object.
	 * 
	 * @param ns Namespace where the enum is defined
	 * @param enumName Name of the enum (retrieved from parent key)
	 * @return Enum object
	 */
	public JavaEnum getEnum(Namespace ns, String enumName) {
		final JavaEnum e = new JavaEnum(ns, enumName, name);
		List<String> enums = property.isArray() ? property.getItems().getEnums() : property.getEnums();
		
		if (property.obj().isArray()) {
			e.setArray();
		}
			
		for (String enumValue : enums) {
			e.addValue(enumValue);
		}
		return e;
	}
}
