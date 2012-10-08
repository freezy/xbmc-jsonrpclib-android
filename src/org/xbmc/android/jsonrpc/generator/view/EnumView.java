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

import org.xbmc.android.jsonrpc.generator.model.Enum;

/**
 * Renders a Java enum.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class EnumView extends AbstractView {
	
	private final static String DISPLAY_ONLY = "Application.Property.Value";
	
	private final Enum e;
	
	public EnumView(Enum e) {
		this.e = e;
	}
	
	public void render(StringBuilder sb, int indent, boolean force) {
		
		// debug
		if (!force && !DISPLAY_ONLY.isEmpty() && !e.getApiType().equals(DISPLAY_ONLY)) {
			return;
		}
		
		// init
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		
		// signature
		sb.append("\n");
		sb.append(prefix).append("public static enum ");
		if (e.isInner()) {
			sb.append(getInnerType(e.getName()));
		} else {
			sb.append(e.getName());
		}
		sb.append(" {\n\n");
		
		// enumns
		for (String enumValue : e.getValues()) {
			sb.append(prefix).append("\t");
			sb.append(enumValue.toUpperCase());
			sb.append("(\"");
			sb.append(enumValue);
			sb.append("\"),\n");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(";\n\n");
		
		// accessors
		sb.append(prefix).append("	private final String name;\n");
		sb.append(prefix).append("	private Tag(String name) {\n");
		sb.append(prefix).append("		this.name = name;\n");
		sb.append(prefix).append("	}\n");
		sb.append(prefix).append("	public String getName() {\n");
		sb.append(prefix).append("		return name;\n");
		sb.append(prefix).append("	}\n");

		sb.append(prefix).append("}\n");
	}

}
