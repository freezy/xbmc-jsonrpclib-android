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
package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

public abstract class AbstractView {
	
	protected String getClassName(Klass klass) {
		if (klass.isInner()) {
			return getInnerType(klass.getName());
		} else {
			return klass.getName().replace(".", "");
		}
	}

	protected String getClassName(Member member) {
		
		if (!member.isEnum()) {
			if (member.getType().isNative()) {
				return getNativeType(member.getType().getName());
			} else if (member.getType().isInner()) {
				return getInnerType(member.getType().getName());
			} else {
				return member.getType().getName();
			}
		} else {
			return getInnerType(member.getEnum().getName());
		}
	}
	// TODO interface param and member
	protected String getClassName(Parameter param) {
		
		if (!param.isEnum()) {
			if (param.getType().isNative()) {
				return getNativeType(param.getType().getName());
			} else if (param.getType().isInner()) {
				return getInnerType(param.getType().getName());
			} else {
				return param.getType().getName();
			}
		} else {
			return getInnerType(param.getEnum().getName());
		}
	}
	
	private String getNativeType(String type) {
		if (type.equals("boolean")) {
			return "Boolean";
		} else if (type.equals("integer")) {
			return "Integer";
		} else if (type.equals("string")) {
			return "String";
		} else {
			throw new IllegalArgumentException("Unknown native type \"" + type + "\".");
		}
	}
	
	private String getInnerType(String type) {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
}
