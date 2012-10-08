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
package org.xbmc.android.jsonrpc.generator.introspect.wrapper;

import java.util.List;

import org.xbmc.android.jsonrpc.generator.introspect.Type;

/**
 * A wrapper that wraps the value of the "type" attribute, since it can be
 * either of:
 * <ul><li>A String defining a native type ("string", "integer", etc)</li>
 *     <li>A {@link Type} object defining an anonymous type</li>
 *     <li>An array of {@link Type}s defining multiple anonymous types</li>
 * </ul>
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class TypeWrapper {
	
	private final String name;
	private final Type obj;
	private final List<Type> array;

	public TypeWrapper(String name) {
		this.name = name;
		obj = null;
		array = null;
	}

	public TypeWrapper(Type obj) {
		this.name = null;
		this.obj = obj;
		this.array = null;
	}

	public TypeWrapper(List<Type> array) {
		this.name = null;
		this.obj = null;
		this.array = array;
	}
	
	public boolean isArray() {
		return array != null;
	}

	public String getName() {
		return name;
	}

	public Type getObj() {
		return obj;
	}

	public List<Type> getArray() {
		return array;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[type:");
		if (array != null) {
			sb.append("array(");
			for (Type type : array) {
				sb.append(type);
				sb.append(",");
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");
		}
		if (name != null) {
			sb.append("native(");
			sb.append(name);
			sb.append(")");
		}
		if (obj != null) {
			sb.append("object(");
			sb.append(obj);
			sb.append(")");
		}
		sb.append("]");
		return sb.toString();
	}
}