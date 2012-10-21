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
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;
import org.xbmc.android.jsonrpc.generator.view.module.IParentModule;

/**
 * Defines the "outer" class, where the classes sit.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Namespace {

	private static final HashMap<String, Namespace> TYPES = new HashMap<String, Namespace>();
	private static final HashMap<String, Namespace> METHODS = new HashMap<String, Namespace>();
	
	private final Map<String, IClassModule> classModules = new HashMap<String, IClassModule>();
	private final Map<String, IClassModule> innerClassModules = new HashMap<String, IClassModule>();
	private IParentModule parentModule = null;
	private IParentModule innerParentModule = null;

	private final String name;
	private final String packageName;
	private final String classSuffix;
	
	private final List<JavaClass> classes = new ArrayList<JavaClass>();
	private final List<JavaEnum> enums = new ArrayList<JavaEnum>();
	private final Set<String> imports = new HashSet<String>();

	public Namespace(String name, String packageName, String classSuffix) {
		this.name = name;
		this.packageName = packageName;
		this.classSuffix = classSuffix;
	}
	
	/**
	 * Retrieves imports for each module and class of the namespace.
	 */
	public void findModuleImports() {
		for (JavaClass klass : classes) {
			klass.findModuleImports(classModules.values(), parentModule);
		}
	}
	
	/**
	 * Recursively computes a list the imports needed by all classes
	 * in this namespace.
	 * @return
	 */
	public Set<String> findImports() {
		for (JavaClass klass : classes) {
			if (klass.isVisible()) {
				imports.addAll(klass.getImports());
			}
		}
		if (!enums.isEmpty()) {
			imports.add("java.util.HashSet");
			imports.add("java.util.Set");
			imports.add("java.util.Arrays");
		}
		return new TreeSet<String>(imports);
	}
	
	/**
	 * Goes through all classes in the namespace and resolves them.
	 */
	public void resolveClasses() {
		final ListIterator<JavaClass> iterator = classes.listIterator();
		while (iterator.hasNext()) {
			iterator.set(JavaClass.resolve(iterator.next()));
		}
	}
	
	public void addClassModule(IClassModule... classModules) {
		for (int i = 0; i < classModules.length; i++) {
			final IClassModule cm = classModules[i];
			if (!this.classModules.containsKey(cm.getClass().getName())) {
				this.classModules.put(cm.getClass().getName(), cm);
			}
		}
	}
	
	public void addInnerClassModule(IClassModule... classModules) {
		for (int i = 0; i < classModules.length; i++) {
			final IClassModule cm = classModules[i];
			if (!this.innerClassModules.containsKey(cm.getClass().getName())) {
				this.innerClassModules.put(cm.getClass().getName(), cm);
			}
		}
	}
	
	public void setParentModule(IParentModule parentModule) {
		this.parentModule = parentModule;
	}
	
	public void setInnerParentModule(IParentModule parentModule) {
		this.innerParentModule = parentModule;
	}

	public void addClass(JavaClass klass) {
		classes.add(klass);
	}

	public void addEnum(JavaEnum e) {
		enums.add(e);
	}
	
	public String getName() {
		return name + classSuffix;
	}
	
	public String getApiName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public List<JavaClass> getClasses() {
		return classes;
	}

	public List<JavaEnum> getEnums() {
		return enums;
	}
	
	public Collection<IClassModule> getClassModules() {
		return classModules.values();
	}
	
	public Collection<IClassModule> getInnerClassModules() {
		return innerClassModules.values();
	}

	public IParentModule getParentModule() {
		return parentModule;
	}
	
	public IParentModule getInnerParentModule() {
		return innerParentModule;
	}

	public void addImport(String i) {
		imports.add(i);
	}
	
	public boolean isEmpty() {
		for (JavaClass klass : classes) {
			if (klass.isVisible()) {
				return false;
			}
		}
		return enums.isEmpty();
	}

	public static Namespace getType(String name, String packageName, String classSuffix) {
		// trim suffixes if provided
		if (name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		if (!TYPES.containsKey(name)) {
			TYPES.put(name, new Namespace(name, packageName, classSuffix));
		}
		return TYPES.get(name);
	}

	public static Collection<Namespace> getTypes() {
		return TYPES.values();
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Namespace> getAll() {
		return CollectionUtils.union(TYPES.values(), METHODS.values());
	}
	
	public static Namespace getMethod(String name, String packageName, String classSuffix) {
		// trim suffixes if provided
		if (name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		if (!METHODS.containsKey(name)) {
			METHODS.put(name, new Namespace(name, packageName, classSuffix));
		}
		return METHODS.get(name);
	}
	
	public static Collection<Namespace> getMethods() {
		return METHODS.values();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.getName().equals(((Namespace)obj).getName());
	}

}
