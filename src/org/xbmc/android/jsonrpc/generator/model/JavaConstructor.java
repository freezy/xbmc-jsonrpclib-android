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
	private final List<JavaAttribute> parameters = new ArrayList<JavaAttribute>();

	public JavaConstructor(JavaClass type) {
		this.type = type;
	}
	
	public boolean hasParameters() {
		return !parameters.isEmpty();
	}

	public void addParameter(JavaAttribute parameter) {
		parameters.add(parameter);
	}

	public List<JavaAttribute> getParameters() {
		return parameters;
	}

	public JavaClass getType() {
		return type;
	}
	
	public void resolve() {
		type = JavaClass.resolveNonNull(type);
		for (JavaAttribute param : parameters) {
			param.resolveType();
		}
	}
	
	/**
	 * Returns a new reference of this constructor. Note that references to
	 * parameters stay the same, only the list and the constructor are cloned.
	 * @return
	 */
	public JavaConstructor copy() {
		final JavaConstructor jc = new JavaConstructor(this.type);
		for (JavaAttribute jp : parameters) {
			jc.addParameter(jp);
		}
		return jc;
	}
	
	/**
	 * Returns true if a constructor has the same amount of parameters per 
	 * type, in the same order.
	 * 
	 * @param jc
	 * @return
	 */
	public boolean hasSameParams(JavaConstructor jc) {
		
		if (parameters.size() != jc.getParameters().size()) {
			return false;
		}
		
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).isEnum() != jc.getParameters().get(i).isEnum()) {
				return false;
			}
			final JavaClass class1 = parameters.get(i).getType();
			final JavaClass class2 = jc.getParameters().get(i).getType();
			
			if (!class1.equals(class2)) {
				return false;
			}
		}
		return true;
		
	}
}
