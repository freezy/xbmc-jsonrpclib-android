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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the "outer" class, where the classes sit.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Namespace {

	private static HashMap<String, Namespace> namespaces = new HashMap<String, Namespace>();

	private final String name;
	private final String packageName;
	private final List<Klass> classes = new ArrayList<Klass>();
	private final List<Enum> enums = new ArrayList<Enum>();
	private final Set<String> imports = new HashSet<String>();

	public Namespace(String name, String packageName) {
		this.name = name;
		this.packageName = packageName;
	}

	public void addClass(Klass klass) {
		classes.add(klass);
	}

	public void addEnum(Enum e) {
		enums.add(e);
	}

	public String getName() {
		return name + "Model";
	}

	public String getPackageName() {
		return packageName;
	}

	public List<Klass> getClasses() {
		return classes;
	}

	public List<Enum> getEnums() {
		return enums;
	}
	
	public Set<String> getImports() {
		for (Klass klass : classes) {
			imports.addAll(klass.getImports());
		}
		return imports;
	}
	
	public void addImport(String i) {
		imports.add(i);
	}

	public static Namespace get(String name, String packageName) {
		// trim suffixes if provided
		if (name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		if (!namespaces.containsKey(name)) {
			namespaces.put(name, new Namespace(name, packageName));
		}
		return namespaces.get(name);
	}

	public static Collection<Namespace> getAll() {
		return namespaces.values();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.name.equals(((Namespace)obj).name);
	}

}
