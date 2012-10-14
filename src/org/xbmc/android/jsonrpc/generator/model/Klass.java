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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines a class in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Klass {

	private final String name;
	private final String apiType;
	private final Namespace namespace;

	private boolean isNative = false;
	private boolean isInner = false;
	private boolean isMultiType = false;
	private boolean isArray = false;
	private boolean isGlobal = false;

	/**
	 * If true, this is just a place holder and the "real" object has yet to be
	 * fetched.
	 */
	private final boolean unresolved;

	private final List<Constructor> constructors = new ArrayList<Constructor>();
	private final List<Member> members = new ArrayList<Member>();
	private final List<Klass> innerTypes = new ArrayList<Klass>();
	private final List<Enum> innerEnums = new ArrayList<Enum>();

	private final Set<String> imports = new HashSet<String>();
	private final static Map<String, Klass> GLOBALS = new HashMap<String, Klass>();

	private Klass arrayType = null;

	public Klass(String apiType) {
		if (apiType == null) {
			throw new IllegalArgumentException("API type must not be null when creating unresolved class references.");
		}
		this.namespace = null;
		this.name = null;
		this.apiType = apiType;
		this.isGlobal = true;
		this.unresolved = true;
	}

	public Klass(Namespace namespace) {
		this(namespace, null, null);
	}

	public Klass(Namespace namespace, String name) {
		this(namespace, name, null);
	}

	public Klass(Namespace namespace, String name, String apiType) {
		this.namespace = namespace;
		this.name = name;
		this.apiType = apiType;
		this.unresolved = false;
		if (apiType != null) {
			GLOBALS.put(apiType, this);
		}
	}

	public static Klass resolve(Klass klass) {
		if (klass.isUnresolved()) {
			if (!GLOBALS.containsKey(klass.apiType)) {
				throw new RuntimeException("Trying to resolve unknown class \"" + klass.apiType + "\".");
			}
			return GLOBALS.get(klass.apiType);
		} else {
			return klass;
		}
	}

	public void addConstructor(Constructor c) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		constructors.add(c);
	}

	public void addMember(Member member) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		members.add(member);
	}

	public void addInnerType(Klass klass) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		innerTypes.add(klass);
	}

	public void addInnerEnum(Enum e) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		innerEnums.add(e);
	}

	public void addImport(String i) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.imports.add(i);
	}

	public boolean hasInnerTypes() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return !innerTypes.isEmpty();
	}

	public boolean hasInnerEnums() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return !innerEnums.isEmpty();
	}

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isNative = isNative;
	}

	public boolean isInner() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return isInner;
	}

	public void setInner(boolean isInner) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isInner = isInner;
	}

	public boolean isMultiType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return isMultiType;
	}

	public void setMultiType(boolean isMultiType) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isMultiType = isMultiType;
	}

	public boolean isArray() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return isArray;
	}

	public void setArray(boolean isArray) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isArray = isArray;
	}

	public Klass getArrayType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return arrayType;
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public void setGlobal(boolean isGlobal) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.isGlobal = isGlobal;
	}

	public void setArrayType(Klass arrayType) {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		this.arrayType = arrayType;
	}

	public String getName() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return name;
	}

	public Namespace getNamespace() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return namespace;
	}

	public String getApiType() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return apiType;
	}

	public List<Constructor> getConstructors() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return constructors;
	}

	public List<Member> getMembers() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return members;
	}

	public List<Klass> getInnerTypes() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return innerTypes;
	}

	public List<Enum> getInnerEnums() {
		if (unresolved) {
			throw new RuntimeException("Unresolved.");
		}
		return innerEnums;
	}

	public boolean isUnresolved() {
		return unresolved;
	}

	public Set<String> getImports() {
		final Set<String> imports = new HashSet<String>();

		imports.addAll(this.imports);
		for (Member m : members) {
			if (m.getType() != null) {
				imports.addAll(m.getType().getImports());
			}
		}
		return imports;
	}

}
