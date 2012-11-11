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

	private String name;
	private final String apiType;
	private final Namespace namespace;
	
	private String description = null;

	private boolean isInner = false; // = !isGlobal
	private Nature nature = null;

	/**
	 * Defines the nature of this type.
	 * @author freezy <freezy@xbmc.org>
	 */
	public enum Nature {
		/**
		 * Native type.
		 * Currently supported is: <tt>integer</tt>, <tt>string</tt>,
		 * <tt>boolean</tt>, <tt>number</tt>.
		 * 
		 * @see JavaClass#getName()
		 */
		NATIVE,
		
		/**
		 * A type that can be multiple different types. For every type, a 
		 * member is used, so all members but one is always null.
		 * @see JavaClass#getMembers()
		 */
		MULTITYPE, 
		
		/**
		 * An array of types.
		 * @see JavaClass#getArrayType()
		 */
		TYPEARRAY, 
		
		/**
		 * An array of enums.
		 * @see JavaClass#getEnumArray()
		 */
		ENUMARRAY, 
		
		/**
		 * A dictionary (or {@link Map}).
		 * Result of <tt>additionalProperties</tt> being set. The key is always
		 * a String, value is defined by mapType.
		 * @see
		 */
		TYPEMAP;
	}

	/**
	 * Parent class, set if property "extends" something.
	 */
	private JavaClass parentClass = null;
	/**
	 * If this is a type array, the type is set here.
	 */
	private JavaClass arrayType = null;
	/**
	 * If this is an enum array, the type is set here.
	 */
	private JavaEnum arrayEnum = null;
	/**
	 * If this is a type map, the type is set here.
	 */
	private JavaClass mapType = null;
	/**
	 * If this is an inner class, the outer class is set here.
	 */
	private JavaClass outerType = null; // set if isInner == true.
	
	/**
	 * True if this is used in any call as parameter and the user needs
	 * to instantiate it (needed so we know we need to be able to construct it 
	 * with its member values).
	 */
	private boolean usedAsParameter = false;
	/**
	 * True if this type is returned by the API and it has to be de-serialized
	 * from an JSON node.
	 */
	private boolean usedAsResult = false;
	
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
	private final List<JavaAttribute> members = new ArrayList<JavaAttribute>();
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
	 * New class as enum array.
	 * 
	 * When defining the model, we don't care about whether an enum is just an
	 * enum or an array of enums, we always render just the enum values. 
	 * However, for parameters and results, this is important. In this case, an
	 * array of enums is represented by a {@link JavaClass} with nature
	 * {@link Nature#ENUMARRAY}.
	 * 
	 * This constructor creates such a class for a given enum.
	 * 
	 * @param arrayEnum Enum of the array
	 */
	public JavaClass(JavaEnum arrayEnum) {
		this.namespace = arrayEnum.getNamespace();
		this.name = arrayEnum.getName();
		this.apiType = arrayEnum.getApiType();
		this.arrayEnum = arrayEnum;
		this.unresolved = false;
		this.nature = Nature.ENUMARRAY;
	}
	
	/**
	 * Returns {@link #resolve()} but fails if class cannot be resolved. Use
	 * this when you're sure you're resolving a class and not an enum.
	 * @param klass The class to be resolved
	 * @return Resolved class.
	 */
	public static JavaClass resolveNonNull(JavaClass klass) {
		return resolve(klass, true);
	}
	
	/**
	 * Returns {@link #resolve()} and returns null if class cannot be resolved.
	 * Use this when it's not clear whether an enum or a type is being resolved
	 * and in the first case {@link JavaEnum#resolve(JavaClass)} is called 
	 * right afterwards.
	 * 
	 * @param klass The class to be resolved
	 * @return Resolved class or null if not found.
	 */
	public static JavaClass resolve(JavaClass klass) {
		return resolve(klass, false);
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
	 * @param klass The class to be resolved
	 * @param fail If true, an exception is thrown, otherwise null is returned.
	 * @return Resolved class
	 */
	public static JavaClass resolve(JavaClass klass, boolean fail) {

		if (klass.resolved) {
			return klass;
		}

		final JavaClass resolvedClass;

		// resolve class itself
		if (klass.isUnresolved()) {
			if (!GLOBALS.containsKey(klass.apiType)) {
				if (fail) {
					throw new IllegalArgumentException("Trying to resolve unknown class \"" + klass.apiType + "\".");
				} else {
					return null;
				}
			}
			resolvedClass = GLOBALS.get(klass.apiType);
			if (klass.hasDescription()) {
				resolvedClass.setDescription(klass.getDescription());
			}
			if (klass.isUsedAsParameter()) {
				resolvedClass.setUsedAsParameter();
			}
			if (klass.isUsedAsResult()) {
				resolvedClass.setUsedAsResult();
			}
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
			parentClass = resolveNonNull(parentClass);
		}

		// and array type
		if (arrayType != null) {
			final JavaClass type = resolve(arrayType);
			if (type == null) {
				arrayEnum = JavaEnum.resolve(arrayType);
				if (arrayEnum == null) {
					throw new IllegalStateException("Cannot resolve member \"" + name + "\" to neither enum nor class.");
				}
				arrayType = null;
				nature = Nature.ENUMARRAY;
			} else {
				arrayType = type;
			}
		}

		// ..and map type
		if (mapType != null) {
			final JavaClass type = resolve(mapType);
			if (type == null) {
				throw new IllegalStateException("Cannot resolve map type \"" + name + "\".");
			}
		}
		
		// inner classes
		final ListIterator<JavaClass> iterator = innerTypes.listIterator();
		while (iterator.hasNext()) {
			iterator.set(JavaClass.resolveNonNull(iterator.next()));
		}
		
		// constructor types
		for (JavaConstructor c : constructors) {
			c.resolve();
		}

		// ..and members
		for (JavaAttribute m : members) {
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
	 * Returns true if the class is a type array. In this case,
	 * {@link #getArrayType()} will return an object.
	 * 
	 * @see #getArrayType()
	 * @see #isEnumArray()
	 * @return True if array, false otherwise.
	 */
	public boolean isTypeArray() {
		return nature == Nature.TYPEARRAY;
	}
	
	/**
	 * Returns true if the class is an enum array. In this case,
	 * {@link #getEnumArray()} will return an object.
	 * 
	 * @see #getArrayType()
	 * @see #isTypeArray()
	 * @return True if type array, false otherwise.
	 */
	public boolean isEnumArray() {
		return nature == Nature.ENUMARRAY;
	}
	
	/**
	 * Returns true if the class is a type map. In this case,
	 * {@link #getMapType()} will return an object.
	 * @return
	 */
	public boolean isTypeMap() {
		return nature == Nature.TYPEMAP;
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
	 * Returns the API type of the class, which is the ID under types objects (e.g.
	 * <tt>List.Filter.Albums</tt>, <tt>Audio.Details.Album</tt>)
	 * @return API name
	 */
	public String getApiType() {
		return apiType;
	}
	
	/**
	 * Marks this class as used as parameter.
	 */
	public void setUsedAsParameter() {
		if (usedAsParameter) {
			return;
		}
		usedAsParameter = true;
		if (parentClass != null) {
			parentClass.setUsedAsParameter();
		}
		if (arrayType != null) {
			arrayType.setUsedAsParameter();
		}
		if (mapType != null) {
			mapType.setUsedAsParameter();
		}
		for (JavaClass c : innerTypes) {
			c.setUsedAsParameter();
		}
		for (JavaAttribute m : members) {
			if (!m.isEnum()) {
				m.getType().setUsedAsParameter();
			}
		}
	}
	
	/**
	 * Marks this class as used as result
	 */
	public void setUsedAsResult() {
		if (usedAsResult) {
			return;
		}
		usedAsResult = true;
		if (parentClass != null) {
			parentClass.setUsedAsResult();
		}
		if (arrayType != null) {
			arrayType.setUsedAsResult();
		}
		if (mapType != null) {
			mapType.setUsedAsResult();
		}
		for (JavaClass c : innerTypes) {
			c.setUsedAsResult();
		}
		for (JavaAttribute m : members) {
			if (!m.isEnum()) {
				m.getType().setUsedAsResult();
			}
		}
	}
	
	/**
	 * Returns true if this type is used as parameter, false otherwise.
	 * @return
	 */
	public boolean isUsedAsParameter() {
		return usedAsParameter;
	}
	
	/**
	 * Returns true if this type is used as result, false otherwise.
	 * @return
	 */
	public boolean isUsedAsResult() {
		return usedAsResult;
	}

	/**
	 * Adds type to inner types and updates the reference back to this instance.
	 * 
	 * @param innerType The inner type linked to this class
	 */
	public void linkInnerType(JavaClass innerType) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
		}
		this.isInner = true;
		this.outerType = outerType;
	}

	public void linkInnerEnum(JavaEnum e) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		innerEnums.add(e);
		e.setInner(this);
	}

	/**
	 * Returns if the class should be rendered or not. Basically native types
	 * and array of native types are not.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return !(isNative() || (isTypeArray() && !arrayType.isVisible()));
	}

	/**
	 * Adds a new class constructor to this class.
	 * 
	 * @param c New class constructor
	 */
	public void addConstructor(JavaConstructor c) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		constructors.add(c);
	}
	
	/**
	 * Sets all class constructors.
	 * @param constructors
	 */
	public void setConstructors(List<JavaConstructor> constructors) {
		this.constructors.clear();
		this.constructors.addAll(constructors);
	}

	/**
	 * Adds a new class member to this class.
	 * 
	 * @param member New class member
	 */
	public void addMember(JavaAttribute member) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
		}
		return !innerEnums.isEmpty();
	}

	/**
	 * Marks the class as a native class.
	 */
	public void setNative() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		nature = Nature.MULTITYPE;
	}

	/**
	 * Marks the class as a type array.
	 * 
	 * @param arrayType Type of the array
	 */
	public void setArray(JavaClass arrayType) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		this.nature = Nature.TYPEARRAY;
		this.arrayType = arrayType;
	}
	
	/**
	 * Marks the class as a type map.
	 * 
	 * @param mapType Type of the map value (key is String)
	 */
	public void setMap(JavaClass mapType) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		if (nature != null) {
			throw new IllegalStateException("Cannot set nature if already set.");
		}
		this.nature = Nature.TYPEMAP;
		this.mapType = mapType;
	}

	/**
	 * Marks the class as a global class.
	 */
	public void setGlobal() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
		}
		return nature == Nature.MULTITYPE;
	}

	/**
	 * Returns the array type. This only returns an non-null object if
	 * {@link #isTypeArray()} is true.
	 * 
	 * @see #isTypeArray()
	 * @return Array type of this class.
	 */
	public JavaClass getArrayType() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return arrayType;
	}
	
	/**
	 * Returns the array enum. This only returns an non-null object if
	 * {@link #isEnumArray()} is true.
	 * 
	 * @see #isEnumArray()
	 * @return Array enum of this class.
	 */
	public JavaEnum getEnumArray() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return arrayEnum;
	}
	
	/**
	 * Returns the map type in case of a dictionary. This only returns a
	 * non-null object if {@link #isTypeMap()} is true. 
	 * @return Map type of this class.
	 */
	public JavaClass getMapType() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return mapType;
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
			throw new IllegalStateException("Unresolved.");
		}
		return name;
	}
	
	/**
	 * Adds a suffix to the name.
	 * 
	 * This is sometime needed if more than one anonymous type is used as
	 * argument and both need to be declared but have the same name.
	 * @param suffix Suffix
	 */
	public void suffixName(String suffix) {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		if (!isInner) {
			throw new IllegalStateException("Can only suffix name for inner classes.");
		}
		name = name + suffix;
	}

	/**
	 * Returns the namespace the class is attached to.
	 * @return Namespace of the class
	 */
	public Namespace getNamespace() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return namespace;
	}

	/**
	 * Returns all added constructors of this class.
	 * @return All class constructors
	 */
	public List<JavaConstructor> getConstructors() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		return constructors;
	}

	/**
	 * Returns all added members of this class.
	 * @return All class members
	 */
	public List<JavaAttribute> getMembers() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
		}
		// sort before return.
		Collections.sort(members, new Comparator<JavaAttribute>() {
			@Override
			public int compare(JavaAttribute o1, JavaAttribute o2) {
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
			throw new IllegalStateException("Unresolved.");
		}
		return innerTypes;
	}

	/**
	 * Returns all added inner enums of this class.
	 * @return All inner enums
	 */
	public List<JavaEnum> getInnerEnums() {
		if (unresolved) {
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
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
			throw new IllegalStateException("Unresolved.");
		}
		return isInner ? namespace.getInnerClassModules() : namespace.getClassModules();
	}
	
	/**
	 * Returns if class has a description.
	 * @return True if class has a description, false otherwise.
	 */
	public boolean hasDescription() {
		return description != null;
	}
	
	/**
	 * Returns if class has a name.
	 * @return True if class has a name, false otherwise.
	 */
	public boolean hasName() {
		return name != null;
	}

	/**
	 * Returns the class description.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the class description.
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
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
			throw new IllegalStateException("Unresolved.");
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
	
	public List<JavaAttribute> getParentMembers() {
		return getParentMembers(false);
	}
	
	/**
	 * Returns the members plus all parent members.
	 * @return
	 */
	private List<JavaAttribute> getParentMembers(boolean includeOwnMembers) {
		if (doesExtend()) {
			final List<JavaAttribute> members = parentClass.getParentMembers(true); 
			if (includeOwnMembers) {
				members.addAll(this.members);
			}
			return members;
		} else {
			if (includeOwnMembers) {
				return this.members;
			} else {
				return new ArrayList<JavaAttribute>(0);
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
			throw new IllegalStateException("Unresolved.");
		}
		final Set<String> imports = new HashSet<String>();

		// own imports
		imports.addAll(this.imports);

		// members
		for (JavaAttribute m : members) {
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
		// map
		if (isTypeMap()) {
			imports.add("java.util.HashMap");
		}		
		return imports;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (nature != null) {
			switch (nature) {
			case ENUMARRAY:
			case TYPEARRAY:
				sb.append("array: ");
				sb.append(arrayType.toString());
				break;
			case TYPEMAP:
				sb.append("map<string, ");
				sb.append(mapType.toString());
				sb.append(">");
			case MULTITYPE:
				sb.append("multi ");
				break;
			case NATIVE:
				sb.append("native: ");
				sb.append(name);
				break;
			}
		} else {
			if (apiType != null) {
				sb.append(apiType);
				sb.append(": ");
			} else {
				sb.append("unknown: ");
			}
			sb.append(name);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JavaClass)) {
			return false;
		}
		final JavaClass c = (JavaClass)obj;
		if (c.name != null && !c.name.equals(name)) {
			return false;
		}
		if (c.isNative() != isNative()) {
			return false;
		}
		if (c.isGlobal() != isGlobal()) {
			return false;
		}
		if (c.isTypeArray() != isTypeArray()) {
			return false;
		}
		if (c.isTypeArray()) {
			return c.getArrayType().equals(getArrayType());
		}
		
		return true;
	}

}
