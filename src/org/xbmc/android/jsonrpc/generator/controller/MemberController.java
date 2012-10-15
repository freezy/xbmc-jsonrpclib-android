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

import org.xbmc.android.jsonrpc.generator.Introspect;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

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
	
	public Member getMember(Namespace namespace, Klass parentType) {
		
		final Property prop = Introspect.find(property);
		final Member member;
		if (prop.isEnum()) {
			final PropertyController pc = new PropertyController(name, prop);
			final Enum e = pc.getEnum(name);
			e.setInner(true);
			member = new Member(name, e);
			
		} else {
			
			final PropertyController pc = new PropertyController(name, property);
			final Klass klass = pc.getClass(namespace, name, parentType);
			
			// check if the prop is another object definition (=> inner type)
			if (property.hasProperties()) {
				klass.setInner(true);
				klass.setOuterType(parentType);
			}
			
			member = new Member(name, klass);
		}
		
		if (prop.getRequired() != null) {
			member.setRequired(prop.getRequired());
		}
		
		return member;
	}

}
