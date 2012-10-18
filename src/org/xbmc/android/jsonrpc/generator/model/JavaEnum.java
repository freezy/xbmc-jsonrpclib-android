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
import java.util.List;

/**
 * Defines an enum in an agnostic way.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaEnum {

	private final String name;
	private final String apiType;
	private final List<String> values = new ArrayList<String>();

	private boolean isInner = false;
	private JavaClass outerType = null; // set if isInner == true

	public JavaEnum(String name, String apiType) {
		this.name = name;
		this.apiType = apiType;
	}

	public void addValue(String value) {
		values.add(value);
	}

	public String getName() {
		return name;
	}

	public String getApiType() {
		return apiType;
	}

	public List<String> getValues() {
		return values;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

	public JavaClass getOuterType() {
		return outerType;
	}

	public void setOuterType(JavaClass outerType) {
		this.outerType = outerType;
	}

}
