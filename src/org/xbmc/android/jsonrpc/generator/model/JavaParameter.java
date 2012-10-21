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
	private final String name;
	private final JavaEnum e;

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
			type = JavaClass.resolve(type);
		}
	}
	
	public boolean isMultitype() {
		return !multitypes.isEmpty();
	}
	
	public boolean isEnum() {
		return e != null;
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
