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
 * Defines a class member in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaMember {

	private JavaClass type;
	private JavaEnum e;
	private final String name;
	private boolean required = false;

	public JavaMember(String name, JavaClass type) {
		if (name == null) {
			throw new IllegalArgumentException("Member name cannot be null.");
		}
		if (type == null) {
			throw new IllegalArgumentException("Member type cannot be null. Use other constructor if you want to set an enum.");
		}
		this.name = name;
		this.type = type;
		this.e = null;
	}
	
	public JavaMember(String name, JavaEnum e) {
		if (name == null) {
			throw new IllegalArgumentException("Member name cannot be null.");
		}
		if (e == null) {
			throw new IllegalArgumentException("Enum cannot be null. Use other constructor if you want to set a class type.");
		}
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public boolean isEnum() {
		return e != null;
	}
	
	public boolean isInner() {
		return type != null && type.isInner();
	}
	
	public boolean isArray() {
		return type != null && type.isArray();
	}
	
	public boolean isGlobal() {
		return type != null && type.isGlobal();
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
	
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public JavaMember resolveType() {
		if (this.type != null) {
			JavaClass type = JavaClass.resolve(this.type);
			// try to resolve as enum
			if (type == null) {
				e = JavaEnum.resolve(this.type);
				if (e == null) {
					throw new IllegalStateException("Cannot resolve member \"" + name + "\" to neither enum nor class.");
				}
			} else {
				this.type = type;
			}
		}
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((JavaMember)obj).getName().equals(name);
	}

}
