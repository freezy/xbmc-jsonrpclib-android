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
 * Defines a class constructor in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaConstructor {

	private JavaClass type;
	private final List<JavaParameter> parameters = new ArrayList<JavaParameter>();

	public JavaConstructor(JavaClass type) {
		this.type = type;
	}
	
	public boolean hasParameters() {
		return !parameters.isEmpty();
	}

	public void addParameter(JavaParameter parameter) {
		parameters.add(parameter);
	}

	public List<JavaParameter> getParameters() {
		return parameters;
	}

	public JavaClass getType() {
		return type;
	}
	
	public void resolve() {
		type = JavaClass.resolveNonNull(type);
		for (JavaParameter param : parameters) {
			param.resolveType();
		}
	}

}
