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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;
import org.xbmc.android.jsonrpc.generator.view.module.IParentModule;

/**
 * Defines a class in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaClass {

	private final String name;
	private final String apiType;
	private final Namespace namespace;

	private boolean isInner = false; // = !isGlobal
	private Nature nature = null;

	public enum Nature {
		NATIVE, MULTITYPE, ARRAY;
	}

	/**
	 * Parent class, set if property "extends" something.
	 */
	private JavaClass parentClass = null;
	/**
	 * If this is an array, the type is set here.
	 */
	private JavaClass arrayType = null;
	/**
	 * If this is an inner class, the outer class is set here.
	 */
	private JavaClass outerType = null; // set if isInner == true.

	/**
	 * If true, this is just a place holder and the "real" object has yet to be
	 * fetched.
	 */
	private final boolean unresolved;
	/**
	 * In order to avoid stack overflow due to circular references, once a class
	 * is resolved, mark it as such.
	 */
	private boolean resolved = false;

	private final List<JavaConstructor> constructors = new ArrayList<JavaConstructor>();
	private final List<JavaMember> members = new ArrayList<JavaMember>();
	private final List<JavaClass> innerTypes = new ArrayList<JavaClass>();
	private final List<JavaEnum> innerEnums = new ArrayList<JavaEnum>();

	private final Set<String> imports = new HashSet<String>();

	/**
	 * Contains all global classes for resolving purpose.
	 */
	private final static Map<String, JavaClass> GLOBALS = new HashMap<String, JavaClass>();

	/**
	 * New class by reference.
	 * 
	 * Only the "id" of the global type is provided. When rendering the class
	 * later, it must be resolved by using {@link #resolve(JavaClass)}.
	 * 
	 * @param apiType Name of the global type ("id" attribute under "types").
	 */
	public JavaClass(String apiType) {
		if (apiType == null) {
			throw new IllegalArgumentException("API type must not be null when creating unresolved class references.");
		}
		this.namespace = null;
		this.name = null;
		this.apiType = apiType;
		this.unresolved = true;
		this.isInner = false;
	}

	/**
	 * New class by namespace only.
	 * 
	 * This happens only for anonymous item types ("Addon.Details" ->
	 * dependencies) where there is neither a parameter name nor a member name.
	 * 
	 * @param namespace Namespace reference
	 */
	public JavaClass(Namespace namespace) {
		this(namespace, null, null);
	}

	/**
	 * New class by namespace and variable name.
	 * 
	 * Another anonymous type, but with a given variable name, retrieved from
	 * property name or parameter name. It could also be a computed name for
	 * multitypes.
	 * 
	 * @param namespace Namespace reference
	 * @param name Best guess of name (will be transformed later depending on
	 *            type)
	 */
	public JavaClass(Namespace namespace, String name) {
		this(namespace, name, null);
	}

	/**
	 * New class for global types.
	 * 
	 * A global type, as defined in introspect's "type" list. The "id" attribute
	 * corresponds to the {@link #apiType} variable.
	 * 
	 * @param namespace Namespace reference
	 * @param name Best guess of name (will be ignored later)
	 * @param apiType Name of global type
	 */
	public JavaClass(Namespace namespace, String name, String apiType) {
		this.namespace = namespace;
		this.name = name;
		this.apiType = apiType;
		this.unresolved = false;
		if (apiType != null) {
			GLOBALS.put(apiType, this);
		}
	}

	/**
	 * Returns the resolved class object if unresolved or the same instance
	 * otherwise.
	 * 
	 * If this class had only a reference to a global type, it was marked as
	 * unresolved. Later, when all global types are transformed into
	 * {@link JavaClass} objects (e.g. when rendering), the reference can be
	 * returned via this method. </p> Note that this also resolves all the sub
	 * types of the class, like the array type and the parent type.
	 * 
	 * @param klass
	 * @return
	 */
	public static JavaClass resolve(JavaClass klass) {

		if (klass.resolved) {
			return klass;
		}

		final JavaClass resolvedClass;

		// resolve class itself
		if (klass.isUnresolved()) {
			if (!GLOBALS.containsKey(klass.apiType)) {
				throw new RuntimeException("Trying to resolve unknown class \"" + klass.apiType + "\".");
			}
			resolvedClass = GLOBALS.get(klass.apiType);
		} else {
			resolvedClass = klass;
		}

		// resolve referenced classes
		resolvedClass.resolve();

		return resolvedClass;
	}

	/**
	 * Resolves classes attached to this class.
	 */
	protected void resolve() {
		resolved = true;

		// resolve parent class
		if (parentClass != null) {
			parentClass = resolve(this.parentClass);
		}

		// ..and array type
		if (arrayType != null) {
			arrayType = resolve(arrayType);
		}

		// inner classes
		final ListIterator<JavaClass> iterator = innerTypes.listIterator();
		while (iterator.hasNext()) {
			iterator.set(JavaClass.resolve(iterator.next()));
		}

		// ..and members
		for (JavaMember m : members) {
			m.resolveType();
		}

	}

	/**
	 * Returns true if the class extends another one, in which case
	 * {@link #getParentClass()} doesn't return null;
	 * 
	 * @see #getParentClass()
	 * @return True if extends, false otherwise.
	 */
	public boolean doesExtend() {
		return parentClass != null;
	}

	/**
	 * Returns true if the class is of a native type.
	 * 
	 * @return True if native, false otherwise.
	 */
	public boolean isNative() {
		return nature == Nature.NATIVE;
	}

	/**
	 * Returnes true if the class is an array. In this case,
	 * {@link #getArrayType()} will return an object.
	 * 
	 * @see #getArrayType()
	 * @return True if array, false otherwise.
	 */
	public boolean isArray() {
		return nature == Nature.ARRAY;
	}

	/**
	 * Returns true if the class is a non-global inner class. In this case,
	 * {@link #getOuterType()} returns an object. Note that {@link #isInner()}
	 * == (!{@link #isGlobal()}).
	 * 
	 * @see #getOuterType()
	 * @return True if inner, false if global.
	 */
	public boolean isInner() {
		return isInner;
	}

	/**
	 * Returns true if the class is a global class. Note that {@link #isInner()}
	 * == (!{@link #isGlobal()}).
	 * 
	 * @return True if global, false if inner.
	 */
	public boolean isGlobal() {
		return !isInner;
	}

	/**
	 * Returns true if this class is unresolved.
	 * 
	 * @return True if unresolved, false if resolved.
	 * @see #resolve(JavaClass)
	 */
	public boolean isUnresolved() {
		return unresolved;
	}

	/**
	 * Adds type to inner types and updates the reference back to this instance.
	 * 
	 * @param innerType The inner type linked to this class
	 */
	public void linkInnerType(JavaClass innerType) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		innerType.setInner(this);
		innerTypes.add(innerType);
	}

	/**
	 * Marks the class as an inner class.
	 * 
	 * @param outerType Outer type that contains the class.
	 */
	public void setInner(JavaClass outerType) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isInner = true;
		this.outerType = outerType;
	}

	public void linkInnerEnum(JavaEnum e) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		innerEnums.add(e);
		e.setOuterType(this);
	}

	/**
	 * Returns if the class should be rendered or not. Basically native types
	 * and array of native types are not.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return !(isNative() || (isArray() && !arrayType.isVisible()));
	}

	/**
	 * Adds a new class constructor to this class.
	 * 
	 * @param c New class constructor
	 */
	public void addConstructor(JavaConstructor c) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		constructors.add(c);
	}

	/**
	 * Adds a new class member to this class.
	 * 
	 * @param member New class member
	 */
	public void addMember(JavaMember member) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		members.add(member);
	}

	/**
	 * Adds a new import to this class.
	 * 
	 * @param i New import
	 */
	public void addImport(String i) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.imports.add(i);
	}

	/**
	 * Returns true if any inner types are available.
	 * 
	 * @return True if inner types available, false otherwise.
	 */
	public boolean hasInnerTypes() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return !innerTypes.isEmpty();
	}

	/**
	 * Returns true if any inner enums are available.
	 * 
	 * @return True if inner enums available, false otherwise.
	 */
	public boolean hasInnerEnums() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return !innerEnums.isEmpty();
	}

	/**
	 * Marks the class as a native class.
	 */
	public void setNative() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		nature = Nature.NATIVE;
	}

	/**
	 * Marks the class as multi-type class.
	 */
	public void setMultiType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		nature = Nature.MULTITYPE;
	}

	/**
	 * Marks the class as an array.
	 * 
	 * @param arrayType Type of the array
	 */
	public void setArray(JavaClass arrayType) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		this.nature = Nature.ARRAY;
		this.arrayType = arrayType;
	}

	/**
	 * Marks the class as a global class.
	 */
	public void setGlobal() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		isInner = false;
	}

	/**
	 * Returns true if the class is a multi-type class.
	 * 
	 * @return True if multi-type, false otherwise.
	 */
	public boolean isMultiType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return nature == Nature.MULTITYPE;
	}

	/**
	 * Returns the array type. This only returns an non-null object if
	 * {@link #isArray()} is true.
	 * 
	 * @see #isArray()
	 * @return Array type of this class.
	 */
	public JavaClass getArrayType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return arrayType;
	}

	/**
	 * Returns the name of the class.
	 * <ul><li>If the class is native, this is the name of the native type (e.g. <tt>boolean</tt>, <tt>string</tt>)</li>
	 *     <li>If the class is global, this is the ID of type (e.g. <tt>Addon.Details</tt>, <tt>Video.Details.Episode</tt>)</li>
	 *     <li>If the class is a multitype, then this is the name of its parameter (e.g. <tt>Broken</tt>, <tt>And</tt>)</li>
	 *  </ul>
	 * @return
	 */
	public String getName() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return name;
	}

	/**
	 * Returns the namespace the class is attached to.
	 * @return Namespace of the class
	 */
	public Namespace getNamespace() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return namespace;
	}

	/**
	 * Returns the API type of the class, which is the ID under types objects (e.g.
	 * <tt>List.Filter.Albums</tt>, <tt>Audio.Details.Album</tt>)
	 * @return API name
	 */
	public String getApiType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return apiType;
	}

	/**
	 * Returns all added constructors of this class.
	 * @return All class constructors
	 */
	public List<JavaConstructor> getConstructors() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return constructors;
	}

	/**
	 * Returns all added members of this class.
	 * @return All class members
	 */
	public List<JavaMember> getMembers() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		// sort before return.
		Collections.sort(members, new Comparator<JavaMember>() {
			@Override
			public int compare(JavaMember o1, JavaMember o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return members;
	}

	/**
	 * Returns all added inner types of this class.
	 * @return All inner types
	 */
	public List<JavaClass> getInnerTypes() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return innerTypes;
	}

	/**
	 * Returns all added inner enums of this class.
	 * @return All inner enums
	 */
	public List<JavaEnum> getInnerEnums() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return innerEnums;
	}

	/**
	 * Returns the outer type. This only returns a non-null object if the class
	 * is an inner class.
	 * 
	 * @see #isInner()
	 * @returns Outer class if available, null otherwise.
	 */
	public JavaClass getOuterType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return outerType;
	}

	/**
	 * Returns the super class. This only returns a non-null object if the
	 * class extends another class.
	 * 
	 * @see #doesExtend()
	 * @return Parent class if available, null otherwise.
	 */
	public JavaClass getParentClass() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return parentClass;
	}

	/**
	 * Marks the class a child class by setting its parent class.
	 * 
	 * @param parentClass Parent class
	 */
	public void setParentClass(JavaClass parentClass) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		if (this.parentClass != null) {
			throw new IllegalStateException("Cannot re-attach an class to a different parent.");
		}
		this.parentClass = parentClass;
	}

	/**
	 * Returns true if the class has a registered render module for rendering
	 * the super class.
	 * @return True if parent render module defined, false otherwise.
	 */
	public boolean hasParentModule() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return getParentModule() != null;
	}

	/**
	 * Returns the appropriate parent render module if available. Note that two
	 * types of parent render modules can be defined: One for the class and
	 * one for inner types.
	 * @return Parent render module
	 */
	public IParentModule getParentModule() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return isInner ? namespace.getInnerParentModule() : namespace.getParentModule();
	}

	/**
	 * Returns the appropriate class render modules. Note that two types of
	 * class render modules can be defined: For class and inner types.
	 * @return List of class render modules
	 */
	public Collection<IClassModule> getClassModules() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return isInner ? namespace.getInnerClassModules() : namespace.getClassModules();
	}

	/**
	 * Applies the import routine of a list of modules to this class and its
	 * inner types.
	 * 
	 * This basically goes through all modules and applies 
	 * {@link IClassModule#getImports(JavaClass)} / {@link IParentModule#getImports(JavaClass)}
	 * to this class and all its inner types. These can then be retrieved by
	 * {@link #getImports()}.
	 * 
	 * @see #getImports()
	 * @see IClassModule#getImports(JavaClass)
	 * @see IParentModule#getImports(JavaClass)
	 * @param modules Class render modules
	 * @param parentModule Parent render module
	 */
	public void findModuleImports(Collection<IClassModule> modules, IParentModule parentModule) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		if (isVisible()) {
			// class render modules
			for (IClassModule module : modules) {
				imports.addAll(module.getImports(this));
				for (JavaClass klass : innerTypes) {
					klass.findModuleImports(namespace.getInnerClassModules(), namespace.getInnerParentModule());
				}
			}
			// superclass render module if available
			if (parentModule != null) {
				imports.addAll(parentModule.getImports(this));
			}
		}
	}
	
	/**
	 * Recursively retrieves all imports from this class and its members,
	 * inner types and enums.
	 * @return All imports of this and all contained classes.
	 */
	public Set<String> getImports() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		final Set<String> imports = new HashSet<String>();

		// own imports
		imports.addAll(this.imports);
		// members
		for (JavaMember m : members) {
			if (!m.isEnum()) {
				imports.addAll(m.getType().getImports());
			}
		}
		// inner types
		for (JavaClass klass : innerTypes) {
			imports.addAll(klass.getImports());
		}
		// enums
		if (!innerEnums.isEmpty()) {
			imports.add("java.util.HashSet");
			imports.add("java.util.Set");
			imports.add("java.util.Arrays");
		}
		return imports;
	}

}
