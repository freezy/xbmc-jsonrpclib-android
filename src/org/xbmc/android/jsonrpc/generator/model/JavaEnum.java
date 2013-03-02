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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xbmc.android.jsonrpc.generator.introspect.Property;

/**
 * Defines an enum in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaEnum {

	private final String name;
	private final Namespace namespace;  
	private final String apiType;
	private final NativeType nativeType;
	private final List<String> values = new ArrayList<String>();
	private final boolean unresolved;

	private boolean isInner = false;
	private boolean isArray = false;
	private JavaEnum parentEnum = null;
	private JavaClass outerType = null; // set if isInner == true
	
	public enum NativeType {
		STRING {
			@Override
			public String toString() {
				return "String";
			}
		}, 
		INTEGER {
			@Override
			public String toString() {
				return "Integer";
			}
		}
	}

	public JavaEnum(Namespace namespace, String name, String apiType, NativeType nativeType) {
		this.name = name;
		this.namespace = namespace;
		this.apiType = apiType;
		this.nativeType = nativeType;
		this.unresolved = false;
		
		if (apiType != null) {
			GLOBALS.put(apiType, this);
		}
	}
	
	public JavaEnum(String apiType) {
		if (apiType == null) {
			throw new IllegalArgumentException("API type must not be null when creating unresolved enum references.");
		}
		this.name = null;
		this.namespace = null;
		this.apiType = apiType;
		this.nativeType = null;
		this.unresolved = true;
	}
	
	/**
	 * Contains all global enums for resolving purpose.
	 */
	private final static Map<String, JavaEnum> GLOBALS = new HashMap<String, JavaEnum>();

	public void addValue(String value) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		values.add(value);
	}

	public String getName() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return name;
	}
	public String getTypeName() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return nativeType.toString();
	}
	
	public Namespace getNamespace() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return namespace;
	}

	public String getApiType() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return apiType;
	}

	public List<String> getValues() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return values;
	}

	public boolean isInner() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return isInner;
	}
	
	public boolean isUnresolved() {
		return unresolved;
	}

	public JavaClass getOuterType() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return outerType;
	}

	public void setInner(JavaClass outerType) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		this.outerType = outerType;
		this.isInner = true;
	}
	
	public JavaEnum getParentEnum() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return parentEnum;
	}

	public void setParentEnum(JavaEnum parentEnum) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		if (this.parentEnum != null) {
			throw new IllegalStateException("Cannot re-attach an enum to a different parent.");
		}
		this.parentEnum = parentEnum;
	}
	
	public boolean doesExtend() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return parentEnum != null;
	}
	
	public JavaEnum setArray() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		this.isArray = true;
		return this;
	}
	
	public boolean isInt() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return nativeType == NativeType.INTEGER;
	}
	
	public boolean isString() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return nativeType == NativeType.STRING;
	}
	
	/**
	 * Global enums sometimes are defined as array of enums. Since we render
	 * them as if they are normal enums when defining, it's important to know
	 * when methods refer to them whether they're array or not.
	 * @return
	 */
	public boolean isArray() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return isArray;
	}
	
	protected void resolve() {
		
		// resolve parent class
		if (parentEnum != null) {
			parentEnum = resolve(parentEnum);
		}
	}
	

	/**
	 * Returns the resolved enum object.
	 * 
	 * It's possible that specially methods contain "unresolved" classes
	 * where there is only a reference. It will be converted to a {@link JavaClass},
	 * but it potentially could also be an {@link JavaEnum}.
	 * 
	 * If {@link JavaClass#resolve(JavaClass)} returns null, this should be
	 * tried.
	 * 
	 * Why not use {@link Property#obj()} to figure out if it's an enum in the
	 * first place? Well, since it would be an empty JavaEnum with only the API
	 * type set, we didn't bother, JavaClass suits the case equally.
	 * 
	 * @param klass
	 * @return
	 */
	public static JavaEnum resolve(JavaClass klass) {

		if (!klass.isUnresolved() || klass.getApiType() == null) {
			return null;
		}
		
		if (!GLOBALS.containsKey(klass.getApiType())) {
			return null;
		}

		return GLOBALS.get(klass.getApiType());
	}
	
	public static JavaEnum resolve(JavaEnum e) {

		final JavaEnum resolvedEnum;

		// resolve enum itself
		if (e.isUnresolved()) {
			if (!GLOBALS.containsKey(e.apiType)) {
				return null;
			}
			resolvedEnum = GLOBALS.get(e.apiType);
		} else {
			resolvedEnum = e;
		}

		// resolve referenced classes
		resolvedEnum.resolve();

		return resolvedEnum;
	}

}
