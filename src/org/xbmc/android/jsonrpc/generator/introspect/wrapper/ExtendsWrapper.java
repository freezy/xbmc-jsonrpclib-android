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

/**
 * Wraps the <tt>extends</tt> attribute.
 * 
 * The attribute can either be a String or a list of Strings. This 
 * class contains either value.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class ExtendsWrapper {
	
	private final String name;
	private final List<String> list;

	public ExtendsWrapper(String name) {
		this.name = name;
		list = null;
	}

	public ExtendsWrapper(List<String> list) {
		this.name = null;
		this.list = list;
	}

	public String getName() {
		return name;
	}
	
	public List<String> getList() {
		return list;
	}

}