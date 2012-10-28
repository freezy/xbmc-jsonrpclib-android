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
	private final List<String> values = new ArrayList<String>();

	private boolean isInner = false;
	private JavaClass outerType = null; // set if isInner == true

	public JavaEnum(Namespace namespace, String name, String apiType) {
		this.name = name;
		this.namespace = namespace;
		this.apiType = apiType;
		
		if (apiType != null) {
			GLOBALS.put(apiType, this);
		}
	}
	
	/**
	 * Contains all global enums for resolving purpose.
	 */
	private final static Map<String, JavaEnum> GLOBALS = new HashMap<String, JavaEnum>();

	public void addValue(String value) {
		values.add(value);
	}

	public String getName() {
		return name;
	}
	
	public Namespace getNamespace() {
		return namespace;
	}

	public String getApiType() {
		return apiType;
	}

	public List<String> getValues() {
		return values;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

	public JavaClass getOuterType() {
		return outerType;
	}

	public void setOuterType(JavaClass outerType) {
		this.outerType = outerType;
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

}
