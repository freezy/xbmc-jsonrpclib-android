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
package org.xbmc.android.jsonrpc.generator.controller;

import org.xbmc.android.jsonrpc.generator.introspect.Method;
import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

import com.sun.xml.internal.bind.v2.schemagen.episode.Klass;

/**
 * Produces a {@link Klass} or {@link Enum} for a given {@link Property}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MethodController {


	private final String name;
	private final String apiType;
	private final Method method;
	
	/**
	 * Creates a new method controller.
	 * @param apiType Full name of the method, e.g.: <tt>Addons.ExecuteAddon</tt>
	 * @param method Unserialized JSON object
	 */
	public MethodController(String apiType, Method method) {
		if (method == null) {
			throw new IllegalArgumentException("Method parameter must not be null.");
		}
		this.method = method;
		this.apiType = apiType;
		this.name = apiType.contains(".") ? apiType.substring(apiType.indexOf(".") + 1) : apiType;
	}
	
	/**
	 * Registers a method.
	 * @param packageName 
	 */
	public void register(String packageName) {
		
		final Namespace ns = Namespace.getMethod(apiType, packageName);
		ns.addMethod(getMethod(ns, name));
	}
	
	/**
	 * Creates the agnostic {@link Klass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public JavaMethod getMethod(Namespace namespace, String methodName) {
		
		final JavaMethod method = new JavaMethod();
		return method;
	}
}
