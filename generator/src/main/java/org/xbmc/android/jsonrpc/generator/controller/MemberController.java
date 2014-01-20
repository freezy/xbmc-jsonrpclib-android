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

import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.model.JavaAttribute;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaEnum;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Produces a {@link JavaAttribute} for a given {@link Property}.
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

	public JavaAttribute getMember(Namespace namespace, JavaClass parentType) {

		final Property obj = property.obj();
		final JavaAttribute member;

		// direct enum
		if (obj.isEnum() || (obj.isArray() && obj.getItems().obj().isEnum())) {

			final PropertyController pc = new PropertyController(name, obj);
			final JavaEnum e = pc.getEnum(namespace, name);
			parentType.linkInnerEnum(e);
			member = new JavaAttribute(name, e, parentType);

		} else {

			final PropertyController pc = new PropertyController(name, property);
			final JavaClass klass = pc.getClass(namespace, name, parentType);
			member = new JavaAttribute(name, klass, parentType);
		}

		if (obj.getRequired() != null) {
			member.setRequired(obj.getRequired());
		}
		member.setDescription(property.getDescription());

		return member;
	}

}
