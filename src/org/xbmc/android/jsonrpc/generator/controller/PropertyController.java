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
			
		} else if (p.isArray() && p.getItems().isEnum()) {
			/* So if we have an array that is made out of enums, it's
			 * really a array of Strings. But still, we should define 
			 * the enums somewhere, so we ignore the type and treat
			 * the array type.
			 */
			final PropertyController pc = new PropertyController(name, type.getItems());
			ns.addEnum(pc.getEnum(strippedName));
		} else {
			ns.addClass(getClass(strippedName));
		}
	}
	
	/**
	 * Creates the agnostic {@link Klass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public Klass getClass(String className) {
		
		final Klass klass;
		
		// create class from native type
		if (property.isNative()) {
			klass = new Klass(property.getType().getName());
			klass.setNative(true);
			
		// create class from multiple values
		} else if (property.isMultitype()) {
			final List<Type> types = property.getType().getList();
			
			klass = new Klass(className);
			klass.setInner(true);
			klass.setMultiType(true);
			int i = 0;
			for (Type t : types) {
				final MemberController mc = new MemberController("arg" + (i++), t);
				klass.addMember(mc.getMember());
			}
			
		// create class from array	
		} else if (property.isArray()) {
			final PropertyController pc = new PropertyController(null, property.getItems());
			klass = new Klass();
			klass.setArray(true);
			final Klass arrayType = pc.getClass(className);
			arrayType.setInner(true);
			klass.setArrayType(arrayType);
			
		// create class from global type
		} else {
			klass = new Klass(className, name);
		}
		
		// parse properties
		if (property.hasProperties()) {
			for (String propertyName : property.getProperties().keySet()) {
				final Property prop = property.getProperties().get(propertyName);
				
				final MemberController mc = new MemberController(propertyName, prop);
				final Member member = mc.getMember();
				
				// if type is inner or enum, reference here so it can properly rendered
				if (member.isInner()) {
					klass.addInnerType(member.getType());
				}
				if (member.isEnum()) {
					klass.addInnerEnum(member.getEnum());
				}
				
				if (member.isArray() && !member.getType().getArrayType().isNative()) {
					klass.addInnerType(member.getType().getArrayType());
				}
				
				klass.addMember(member);
			}
		}
		
		// create constructor(s)
		final ConstructorController cc = new ConstructorController(klass);
		for (Constructor c : cc.getConstructors()) {
			klass.addConstructor(c);
		}
		
		return klass;
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
