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

import java.util.Iterator;
import java.util.List;

import org.xbmc.android.jsonrpc.generator.introspect.Type;

/**
 * A wrapper that wraps the value of the "type" attribute, since it can be
 * either of:
 * <ul><li>A String defining a native type ("string", "integer", etc)</li>
 *     <li>A {@link Type} object defining an anonymous type</li>
 *     <li>An list of {@link Type}s defining multiple anonymous types</li>
 * </ul>
 * 
 * This class contains either value.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class TypeWrapper {
	
	private final String name;
	private final Type obj;
	private final List<Type> list;

	public TypeWrapper(String name) {
		this.name = name;
		obj = null;
		list = null;
	}

	public TypeWrapper(Type obj) {
		this.name = null;
		this.obj = obj;
		this.list = null;
	}

	public TypeWrapper(List<Type> list) {
		// trim "null" types (wtf!)
		final Iterator<Type> i = list.iterator();
		while (i.hasNext()) {
			final Type type = i.next();
			final TypeWrapper tr = type.getType();
			if (tr != null && tr.isNative() && tr.getName().equals("null")) {
				i.remove();
			}
		}
		if (list.size() == 1) {
			final Type type = list.get(0);
			if (type.isNative()) {
				this.name = type.getType().getName();
				this.obj = null;
				this.list = null;
			} else {
				this.name = null;
				this.obj = type;
				this.list = null;
			}
		} else {
			this.name = null;
			this.obj = null;
			this.list = list;
		}
		
	}
	
	public boolean isList() {
		return list != null;
	}
	
	public boolean isNative() {
		return name != null;
	}
	
	public boolean isObject() {
		return obj != null;
	}

	public String getName() {
		return name;
	}

	public Type getObj() {
		return obj;
	}

	public List<Type> getList() {
		return list;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[type:");
		if (list != null) {
			sb.append("list(");
			for (Type type : list) {
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