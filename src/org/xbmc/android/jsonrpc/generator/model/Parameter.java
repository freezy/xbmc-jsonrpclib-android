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

/**
 * Defines a method parameter in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Parameter {

	private Klass type;
	private final String name;
	private final Enum e;

	private String description;

	public Parameter(String name, Klass type) {
		this.name = name;
		this.type = type;
		this.e = null;
	}

	public Parameter(String name, Enum e) {
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public Parameter resolve() {
		if (type != null) {
			type = Klass.resolve(type);
		}
		return this;
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

	public Klass getType() {
		return type;
	}

	public Enum getEnum() {
		return e;
	}
	
}
