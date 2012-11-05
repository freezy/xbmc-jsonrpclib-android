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

import java.util.List;

import org.xbmc.android.jsonrpc.generator.introspect.Property;
import org.xbmc.android.jsonrpc.generator.introspect.Type;

public class Helper {
	
	/**
	 * Returns true if a list of types have the same native type.
	 * @param types
	 * @return
	 */
	public static boolean equalNativeTypes(List<Type> types) {
		if (!types.get(0).obj().isNative()) {
			return false;
		}
		final String name = types.get(0).obj().getType().getName();
		for (int i = 1; i < types.size(); i++) {
			final Property type = types.get(i).obj();
			if (!type.isNative() || !type.getType().getName().equals(name)) {
				return false;
			}
		}
		return true;
	}
}
