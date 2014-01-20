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

import org.xbmc.android.jsonrpc.generator.model.JavaEnum;

/**
 * Renders a Java enum.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class EnumView extends AbstractView {
	
	private final JavaEnum e;
	
	public EnumView(JavaEnum e) {
		this.e = e;
	}
	
	public void render(StringBuilder sb, int idt, boolean force) {
		
		// init
		final String indent = getIndent(idt);
		
		// class header comment
		sb.append("\n");
		sb.append(indent).append("/**\n");
		if (e.getApiType() != null) {
			sb.append(indent).append(" * API Name: <tt>");
			sb.append(e.getApiType());
			sb.append("</tt>\n");
		}
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("public interface ");
		sb.append(getEnumName(e));
		if (e.doesExtend()) {
			sb.append(" extends ");
			sb.append(getEnumName(e.getParentEnum()));
		}
		sb.append(" {\n\n");
		
		// enums
		for (String enumValue : e.getValues()) {
			sb.append(indent).append("	public final ");
			sb.append(e.getTypeName());
			sb.append(" ");
			sb.append(getName(enumValue));
			sb.append(" = ");
			if (e.isString()) {
				sb.append("\"");
			}
			sb.append(enumValue);
			if (e.isString()) {
				sb.append("\"");
			}
			sb.append(";\n");
		}
		
		// array public final Set<String> values = new HashSet<String>(Arrays.asList(Type.UNKNOWN, Type.XBMC_ADDON_AUDIO));
		sb.append("\n");
		sb.append(indent).append("	public final static Set<").append(e.getTypeName()).append("> values = new HashSet<").append(e.getTypeName()).append(">(Arrays.asList(");
		if (!e.getValues().isEmpty()) {
			for (String enumValue : e.getValues()) {
				sb.append(getName(enumValue));
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			sb.append("));\n");
		}
		sb.append(indent).append("}\n");
	}
	
	private String getName(String enumValue) {
		if (e.isString()) {
			return enumValue.replaceAll("\\.",  "_").toUpperCase();
		} else {
			if (enumValue.equals("0")) {
				return "ZERO";
			}
			if (enumValue.startsWith("-")) {
				return "MINUS_" + enumValue.substring(1);
			} else {
				return "PLUS_" + enumValue;
			}
		}
	}

}
