package org.xbmc.android.jsonrpc.generator.controller;

import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
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
	 * Name of the property.
	 * 
	 * Since most of the time, the name comes from the key of its parent,
	 * it's not saved in the property itself (exception: {@link Param}).
	 * In the latter case, the name is read from the Param, which means that
	 * {@link #name} is never null.
	 */
	private final String name;
	private final Property property;
	
	public PropertyController(String name, Property property) {
		if (property == null) {
			throw new IllegalArgumentException("Property parameter must not be null.");
		}
		if (!(property instanceof Param) && name == null) {
			throw new IllegalArgumentException("Name parameter can only be null when providing a Param.");
		}
		this.property = property;
		this.name = name != null ? name : ((Param)property).getName();
	}
	
	/**
	 * Registers a global type or enum that is declared in the 
	 * model package.
	 */
	public void register() {
		final Namespace ns = Namespace.get(name);
		
		// class/enum name
		final String strippedName = name.contains(".") ? name.substring(name.indexOf(".") + 1) : name;
		
		if (property.isEnum()) {
			ns.addEnum(getEnum(strippedName));
		} else {
			ns.addClass(getClass(strippedName));
		}
	}
	
	public Klass getClass(String className) {
		
		final Klass klass;
		if (property.isNative()) {
			klass = new Klass(property.getType().getName(), property.getType().getName());
			klass.setNative(true);
		} else {
			klass = new Klass(className, name);
		}
		
		// parse properties
		if (property.isObjectDefinition()) {
			for (String propertyName : property.getProperties().keySet()) {
				final Property prop = property.getProperties().get(propertyName);
				final MemberController mc = new MemberController(propertyName, prop);
				
				final Member member = mc.getMember();
				if (member.getType().isInner()) {
					klass.addInnerType(member.getType());
				}
				
				klass.addMember(member);
			}
		}
		return klass;
	}
	
	public Enum getEnum(String enumName) {
		final Enum e = new Enum(enumName, name);
		return e;
	}
}
