package org.xbmc.android.jsonrpc.generator.controller;

import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;

/**
 * Produces a {@link Member} for a given {@link Property}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MemberController {

	private final Property property;
	private final String name;
	
	public MemberController(String name, Property property) {
		this.property = property;
		this.name = name;
	}
	
	public Member getMember() {
		final Property prop = Introspect.find(property);
		final PropertyController pc = new PropertyController(name, prop);
		
		if (prop.isEnum()) {
			final Enum e = pc.getEnum(name);
			e.setInner(true);
			
			return new Member(name, e);
			
		} else {
			final Klass klass = pc.getClass(name);
			
			// check if the prop is another object definition (=> inner type)
			if (prop.isObjectDefinition()) {
				klass.setInner(true);
			}
			
			return new Member(name, klass);
		}
	}

}
