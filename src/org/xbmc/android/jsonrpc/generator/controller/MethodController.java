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
import org.xbmc.android.jsonrpc.generator.introspect.Param;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.JavaParameter;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Produces a {@link JavaMethod} for a given {@link Method}.
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
	 * Creates the agnostic {@link JavaClass} object.
	 * 
	 * @param className Name of the class (retrieved from parent key)
	 * @return Class object
	 */
	public JavaMethod getMethod(Namespace namespace, String methodName) {
		
		final JavaMethod m = new JavaMethod(name, method.getDescription());
		
		// parameters
		for (Param p : method.getParams()) {
			if (!p.isEnum()) {
				
				final PropertyController pc = new PropertyController(name, p);
				final JavaClass klass = pc.getClass(namespace, name, m);
				m.addParameter(new JavaParameter(p.getName(), klass));
				
/*				final TypeWrapper tr = p.getType();
				if (tr.isNative()) {

					final PropertyController pc = new PropertyController(name, p);
					final JavaClass klass = pc.getClass(namespace, name, m);
					m.addParameter(new JavaParameter(p.getName(), klass));
				} else if (tr.isObject()) {
					final PropertyController pc = new PropertyController(name, tr.getObj());
					final JavaClass klass = pc.getClass(namespace, name, m);
					m.addParameter(new JavaParameter(p.getName(), klass));
				
				} else {
					final List<JavaClass> types = new ArrayList<JavaClass>(tr.getList().size());
					for (Type type : tr.getList()) {
						final PropertyController pc = new PropertyController(name, type);
						types.add(pc.getClass(namespace, name, m));
					}
					m.addParameter(new JavaParameter(p.getName(), types));
				}
*/
			}
		}
		

		// return type
		final TypeWrapper returnType = method.getReturns();
		if (returnType.isObject()) {
			
			final PropertyController returnTypeController = new PropertyController(null, method.getReturns().getObj());
			m.setReturns(returnTypeController.getClass(namespace, name + "ReturnType", m));
			
		} else {
			// TODO
		}
		
		
		return m;
	}
}
