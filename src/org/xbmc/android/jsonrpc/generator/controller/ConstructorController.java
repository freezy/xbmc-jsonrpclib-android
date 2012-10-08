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

import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

public class ConstructorController {

	private final Klass type;
	
	public ConstructorController(Klass type) {
		this.type = type;
	}
	
	public List<Constructor> getConstructors() {
		final List<Constructor> constructors = new ArrayList<Constructor>();
		
		// for non-multitype, just create one constructor with all properties
		if (!type.isMultiType()) {
			final Constructor c = new Constructor(type);
			for (Member m : type.getMembers()) {
				if (m.isEnum()) {
					c.addParameter(new Parameter(m.getName(), m.getEnum()));
				} else {
					c.addParameter(new Parameter(m.getName(), m.getType()));
				}
			}
			constructors.add(c);
			
		// for multitypes, we need a constructor per type (member)	
		} else {
			
			for (Member m : type.getMembers()) {
				final Constructor c = new Constructor(type);
				if (m.isEnum()) {
					c.addParameter(new Parameter(m.getName(), m.getEnum()));
				} else {
					c.addParameter(new Parameter(m.getName(), m.getType()));
				}
				constructors.add(c);
			}
		}
		
		return constructors;
	}
}
