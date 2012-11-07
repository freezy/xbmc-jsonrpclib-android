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
 * Defines a variable.
 * <p>
 * A variable has a name and declares either:
 * <ul><li>A type</li>
 *     <li>An enum</li>
 *     <li>A list of types (multitype)</li>
 * </ul>
 * Plus some additional members such as <tt>description</tt> or <tt>required</tt>.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaAttribute {

	// variable name
	private final String name;
	
	// those 3 are exclusive
	private JavaClass type;
	private JavaEnum e;
	private final List<JavaClass> multitypes = new ArrayList<JavaClass>();
	
	// additional attributes
	private boolean required = false;
	private String description;
	
	/**
	 * Construct with type
	 * @param name Variable name
	 * @param type Associated type
	 */
	public JavaAttribute(String name, JavaClass type) {
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

	/**
	 * Cosntruct with enum
	 * @param name Variable name
	 * @param e Associated enum
	 */
	public JavaAttribute(String name, JavaEnum e) {
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

	/**
	 * Cosntruct with multitypes
	 * @param name Variable name
	 * @param multitypes List of types defining multitype
	 */
	public JavaAttribute(String name, List<JavaClass> multitypes) {
		this.name = name;
		this.e = null;
		this.type = null;
		this.multitypes.addAll(multitypes);
	}
	
	/**
	 * Resolves the type (if not enum)
	 * @return
	 */
	public void resolveType() {
		if (type != null) {
			JavaClass t = JavaClass.resolve(type);
			// try to resolve as enum
			if (t == null) {
				JavaEnum e = JavaEnum.resolve(type);
				if (e == null) {
					throw new IllegalStateException("Cannot resolve argument \"" + name + "\" to neither enum nor class.");
				}
				// if array, upate type.
				if (e.isArray()) {
					type = new JavaClass(e);

				// otherwise it's an enum, so reset type.
				} else {
					this.e = e;
					this.type = null;
				}
			} else {
				type = t;
			}
		}
	}
	
	/**
	 * Returns true if multitype, false othewise.
	 * @return
	 */
	public boolean isMultitype() {
		return !multitypes.isEmpty();
	}
	
	/**
	 * Returns true if enum, false othewise.
	 * @return
	 */
	public boolean isEnum() {
		return e != null;
	}
	
	/**
	 * Returns true if the variable is a type (not enum) and the type is an 
	 * inner type (not global).
	 * @return
	 */
	public boolean isInner() {
		return type != null && type.isInner();
	}
	
	/**
	 * Returns true if the variable is a type (not enum) and the type is an
	 * array.
	 * @return
	 */
	public boolean isArray() {
		return (type != null && (type.isEnumArray() || type.isTypeArray())) || (e != null && e.isArray()); 
	}
	
	/**
	 * Returns true if the variable is a type (not enum) and the type is a 
	 * global type (not inner).
	 * @return
	 */
	public boolean isGlobal() {
		return type != null && type.isGlobal();
	}

	/**
	 * Returns true if description is set.
	 * @return
	 */
	public boolean hasDescription() {
		return description != null;
	}
	
	/**
	 * Returns the description or null if not set.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the name of the variable.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of the variable.
	 * @see #isEnum()
	 * @return
	 */
	public JavaClass getType() {
		return type;
	}

	/**
	 * Returns the enum of the variable.
	 * @see #isEnum()
	 * @return
	 */
	public JavaEnum getEnum() {
		return e;
	}

	/**
	 * Returns true if the variable is marked as "required", in case of a
	 * parameter.
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Sets the required flag.
	 * @param required
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	/**
	 * Compares another variable by name.
	 */
	@Override
	public boolean equals(Object obj) {
		return ((JavaAttribute)obj).getName().equals(name);
	}
}
