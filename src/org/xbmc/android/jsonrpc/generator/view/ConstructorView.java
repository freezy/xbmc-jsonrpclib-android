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

import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

/**
 * Renders a Java class constructor.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class ConstructorView extends AbstractView {

	private final Constructor constructor;

	public ConstructorView(Constructor constructor) {
		this.constructor = constructor;
	}

	public String renderDeclaration(int indent) {

		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}

		final StringBuilder sb = new StringBuilder("\n");
		
		// signature
		sb.append(prefix).append("public ");
		sb.append(getClassName(constructor.getType()));
		sb.append("(");
		for (Parameter p : constructor.getParameters()) {
			sb.append(getClassName(p));
			sb.append(" ");
			sb.append(p.getName());
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(") {\n");
		
		// body
		String lastArg = null;
		for (Parameter p : constructor.getParameters()) {
			sb.append(prefix).append("\tthis.");
			sb.append(p.getName());
			sb.append(" = ");
			sb.append(p.getName());
			sb.append(";\n");
			lastArg = p.getName();
		}
		
		// if multi type, init non-used vars as null
		if (constructor.getType().isMultiType()) {
			for (Member member : constructor.getType().getMembers()) {
				// all but the one we already have.
				if (lastArg != null && lastArg.equals(member.getName())) {
					continue;
				}
				sb.append(prefix).append("\tthis.");
				sb.append(member.getName());
				sb.append(" = null;\n");
			}
		}
		
		
		sb.append(prefix).append("}\n");

		return sb.toString();
	}
}
