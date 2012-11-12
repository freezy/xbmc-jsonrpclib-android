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
 * Since methods are modelized with classes, this is a subclass of
 * {@link JavaClass}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JavaMethod extends JavaClass {

	private JavaClass returnType;
	private String returnProperty = null;
	
	public JavaMethod(Namespace namespace, String name, String apiType) {
		super(namespace, name, apiType);
		usedAsMethod = true;
	}

	public JavaClass getReturnType() {
		return returnType;
	}

	public void setReturnType(JavaClass returnType) {
		this.returnType = returnType;
		this.returnType.setUsedAsResult();
	}
	
	public boolean hasReturnProperty() {
		return returnProperty != null;
	}
	
	public String getReturnProperty() {
		return returnProperty;
	}

	public void setReturnProperty(String returnProperty) {
		this.returnProperty = returnProperty;
	}

	@Override
	public boolean isGlobal() {
		return true;
	}

	@Override
	protected void resolve() {
		super.resolve();
		if (returnType != null) {
			returnType = resolveNonNull(returnType);
		}
	}
}
