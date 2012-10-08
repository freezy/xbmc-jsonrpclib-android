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
package org.xbmc.android.jsonrpc.generator.introspect;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

/**
 * A method is what's defined under the <tt>result</tt>'s <tt>methods</tt>
 * object. It represents a method in XBMC's JSON-RPC API.
 * 
 * @author freezy <freezy@xbmc.org>
 */
@JsonIgnoreProperties( { "type" }) // type is always "method"
public class Method {

	private String description;
	private List<Param> params;
	private TypeWrapper returns;

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Param> getParams() {
		return params;
	}
	public void setParams(List<Param> params) {
		this.params = params;
	}
	public TypeWrapper getReturns() {
		return returns;
	}
	public void setReturns(TypeWrapper returns) {
		this.returns = returns;
	}
}
