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
import java.util.List;

import org.xbmc.android.jsonrpc.generator.model.JavaAttribute;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;

/**
 * Produces a list of {@link JavaConstructor}s for a given {@link JavaClass}.
 *  
 * @author freezy <freezy@xbmc.org>
 */
public class ConstructorController {

	private final JavaClass type;
	
	public ConstructorController(JavaClass type) {
		this.type = type;
	}
	
	public List<JavaConstructor> getConstructors() {
		final List<JavaConstructor> constructors = new ArrayList<JavaConstructor>();
		
		// for non-multitype, just create one constructor with all properties
		if (!type.isMultiType()) {
			final JavaConstructor c = new JavaConstructor(type);
			for (JavaAttribute m : type.getMembers()) {
				if (m.isEnum()) {
					c.addParameter(new JavaAttribute(m.getName(), m.getEnum(), type));
				} else {
					c.addParameter(new JavaAttribute(m.getName(), m.getType(), type));
				}
			}
			constructors.add(c);
			
		// for multitypes, we need a constructor per type (member)	
		} else {
			
			for (JavaAttribute m : type.getMembers()) {
				final JavaConstructor c = new JavaConstructor(type);
				if (m.isEnum()) {
					c.addParameter(new JavaAttribute(m.getName(), m.getEnum(), type));
				} else {
					c.addParameter(new JavaAttribute(m.getName(), m.getType(), type));
				}
				constructors.add(c);
			}
		}
		
		return constructors;
	}
}
