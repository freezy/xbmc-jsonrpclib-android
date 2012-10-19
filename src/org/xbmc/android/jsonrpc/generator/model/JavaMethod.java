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
 * Defines a method modelized in Java.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaMethod implements IClassContainer {

	private final String name;
	private final String description;

	private final List<JavaParameter> params = new ArrayList<JavaParameter>();
	private JavaClass returns;

	public JavaMethod(String name) {
		this(name, null);
	}

	public JavaMethod(String name, String description) {
		this.name = name.replace(".", "");
		this.description = description;
	}
	
	public void addParameter(JavaParameter param) {
		this.params.add(param);
	}

	public JavaClass getReturns() {
		return returns;
	}

	public void setReturns(JavaClass returns) {
		this.returns = returns;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<JavaParameter> getParams() {
		return params;
	}

}
