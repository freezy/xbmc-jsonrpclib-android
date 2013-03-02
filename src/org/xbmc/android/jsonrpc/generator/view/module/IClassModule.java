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
package org.xbmc.android.jsonrpc.generator.view.module;

import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * A module that provides additional rendering to a class.
 * 
 * @author freezy <freezy@hosts.ch>
 */
public interface IClassModule {

	/**
	 * Renders whatever the module renders.
	 * 
	 * @param sb String buffer to append the rendering to.
	 * @param ns Current namespace
	 * @param klass Class to render with
	 * @param indent Indentation for the rendering
	 */
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int indent);
	
	/**
	 * Returns the necessary imports for a class.
	 * 
	 * @param klass Given class
	 * @return List of imports
	 */
	public Set<String> getImports(JavaClass klass);
	
}
