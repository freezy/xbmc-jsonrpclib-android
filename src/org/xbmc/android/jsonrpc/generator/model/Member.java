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

/**
 * Defines a class member in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class Member {

	private final String name;
	private final Klass type;
	private final Enum e;

	public Member(String name, Klass type) {
		this.name = name;
		this.type = type;
		this.e = null;
	}
	
	public Member(String name, Enum e) {
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public boolean isEnum() {
		return e != null;
	}
	
	public boolean isInner() {
		return type != null && type.isInner();
	}

	public String getName() {
		return name;
	}

	public Klass getType() {
		return type;
	}
	
	public Enum getEnum() {
		return e;
	}

}
