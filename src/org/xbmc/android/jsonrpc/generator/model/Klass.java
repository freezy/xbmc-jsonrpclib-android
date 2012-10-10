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
import java.util.HashSet;
import java.util.List;
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

	private final List<Constructor> constructors = new ArrayList<Constructor>();
	private final List<Member> members = new ArrayList<Member>();
	private final List<Klass> innerTypes = new ArrayList<Klass>();
	private final List<Enum> innerEnums = new ArrayList<Enum>();
	
	private final Set<String> imports = new HashSet<String>();
	
	private Klass arrayType = null;

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
	}

	public void addConstructor(Constructor c) {
		constructors.add(c);
	}

	public void addMember(Member member) {
		members.add(member);
	}

	public void addInnerType(Klass klass) {
		innerTypes.add(klass);
	}

	public void addInnerEnum(Enum e) {
		innerEnums.add(e);
	}
	
	public void addImport(String i) {
		this.imports.add(i);
	}

	public boolean hasInnerTypes() {
		return !innerTypes.isEmpty();
	}

	public boolean hasInnerEnums() {
		return !innerEnums.isEmpty();
	}

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

	public boolean isMultiType() {
		return isMultiType;
	}

	public void setMultiType(boolean isMultiType) {
		this.isMultiType = isMultiType;
	}

	public boolean isArray() {
		return isArray;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
	
	public Klass getArrayType() {
		return arrayType;
	}

	public void setArrayType(Klass arrayType) {
		this.arrayType = arrayType;
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

	public List<Constructor> getConstructors() {
		return constructors;
	}

	public List<Member> getMembers() {
		return members;
	}

	public List<Klass> getInnerTypes() {
		return innerTypes;
	}

	public List<Enum> getInnerEnums() {
		return innerEnums;
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
