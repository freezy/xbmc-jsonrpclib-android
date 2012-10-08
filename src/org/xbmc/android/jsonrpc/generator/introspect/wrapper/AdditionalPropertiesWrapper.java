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

public class AdditionalPropertiesWrapper {
	
	private final Boolean available;
	private final TypeWrapper type;
	
	public AdditionalPropertiesWrapper(Boolean available) {
		this.available = available;
		this.type = null;
	}
	
	public AdditionalPropertiesWrapper(TypeWrapper type) {
		this.available = null;
		this.type = type;
	}

	public Boolean getAvailable() {
		return available != null && available;
	}

	public TypeWrapper getType() {
		return type;
	}
}
