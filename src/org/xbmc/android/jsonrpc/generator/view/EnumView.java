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
	
	public final static String DISPLAY_ONLY = "";
	
	private final JavaEnum e;
	
	public EnumView(JavaEnum e) {
		this.e = e;
	}
	
	public void render(StringBuilder sb, int indent, boolean force) {
		
		// debug
		if (!force && !DISPLAY_ONLY.isEmpty() && !e.getApiType().equals(DISPLAY_ONLY)) {
			return;
		}
		
		// init
		final String prefix = getIndent(indent);
		
		// signature
		sb.append("\n");
		sb.append(prefix).append("public interface ");
		sb.append(getEnumName(e));
		sb.append(" {\n\n");
		
		// enumns
		for (String enumValue : e.getValues()) {
			sb.append(prefix).append("	public final ");
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
		sb.append(prefix).append("	public final static Set<").append(e.getTypeName()).append("> values = new HashSet<").append(e.getTypeName()).append(">(Arrays.asList(");
		if (!e.getValues().isEmpty()) {
			for (String enumValue : e.getValues()) {
				sb.append(getName(enumValue));
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			sb.append("));\n");
		}
		sb.append(prefix).append("}\n");
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
	
	
	/**
	 * Renders as a Java enum.
	 * 
	 * @deprecated Model uses enum heritate which isn't possible in Java
	 * @param sb
	 * @param indent
	 * @param force
	 */
	public void renderEnum(StringBuilder sb, int indent, boolean force) {
		
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
		sb.append(getEnumName(e));
		sb.append(" {\n\n");
		
		// enumns
		for (String enumValue : e.getValues()) {
			sb.append(prefix).append("\t");
			sb.append(enumValue.replaceAll("\\.",  "_").toUpperCase());
			sb.append("(\"");
			sb.append(enumValue);
			sb.append("\"),\n");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(";\n\n");
		
		// accessors
		sb.append(prefix).append("	private final String name;\n");
		sb.append(prefix).append("	private ").append(getEnumName(e)).append("(String name) {\n");
		sb.append(prefix).append("		this.name = name;\n");
		sb.append(prefix).append("	}\n");
		sb.append(prefix).append("	public String getName() {\n");
		sb.append(prefix).append("		return name;\n");
		sb.append(prefix).append("	}\n");

		sb.append(prefix).append("}\n");
	}

}
