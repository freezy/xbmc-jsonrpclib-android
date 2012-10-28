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
package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a method parameter in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaParameter {

	private JavaClass type;
	private JavaEnum e;
	private final String name;

	private String description;
	
	private final List<JavaClass> multitypes = new ArrayList<JavaClass>();

	public JavaParameter(String name, JavaClass type) {
		this.name = name;
		this.type = type;
		this.e = null;
	}

	public JavaParameter(String name, JavaEnum e) {
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public JavaParameter(String name, List<JavaClass> multitypes) {
		this.name = name;
		this.e = null;
		this.type = null;
		this.multitypes.addAll(multitypes);
	}
	
	public void resolveType() {
		if (type != null) {
			JavaClass t = JavaClass.resolve(type);
			// try to resolve as enum
			if (t == null) {
				e = JavaEnum.resolve(type);
				if (e == null) {
					throw new IllegalStateException("Cannot resolve member \"" + name + "\" to neither enum nor class.");
				}
				// since it's an enum, reset type.
				type = null;
			} else {
				type = t;
			}
		}
	}
	
	public boolean isMultitype() {
		return !multitypes.isEmpty();
	}
	
	public boolean isEnum() {
		return e != null;
	}
	
	public boolean isArray() {
		return type != null && (type.isEnumArray() || type.isTypeArray()); 
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public JavaClass getType() {
		return type;
	}

	public JavaEnum getEnum() {
		return e;
	}
	
}
