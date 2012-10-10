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

import java.util.Calendar;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Renders a Java outer class.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class NamespaceView extends AbstractView {
	
	//public final static String DISPLAY_ONLY = "Addon";
	public final static String DISPLAY_ONLY = "";
	
	private final Namespace namespace;
	
	public NamespaceView(Namespace namespace) {
		this.namespace = namespace;
	}
	
	public void render(StringBuilder sb) {
		
		// debug
		if (!DISPLAY_ONLY.isEmpty() && !namespace.getName().equals(DISPLAY_ONLY)) {
			return;
		}
		
		// init
		sb.append(GPL_HEADER);
		
		// package
		sb.append("package ").append(namespace.getPackageName()).append(";\n");
		
		// imports
		final Set<String> imports = namespace.getImports();
		if (!imports.isEmpty()) {
			sb.append("\n");
			for (String i : imports) {
				sb.append("import ");
				sb.append(i);
				sb.append(";\n");
			}
		}
		
		// signature
		sb.append("\n");
		sb.append("public final class ");
		sb.append(namespace.getName());
		sb.append(" {\n");
		
		// classes
		for (Klass klass : namespace.getClasses()) {
			final ClassView classView = new ClassView(klass);
			classView.renderDeclaration(sb, 1, false);
		}
		
		// enum
		for (Enum e : namespace.getEnums()) {
			final EnumView enumView = new EnumView(e);
			enumView.render(sb, 1, false);
		}
		
		sb.append("}\n");
	}
	
	private static final String GPL_HEADER = 
			"/*\n" +
			" *      Copyright (C) 2005-" + Calendar.getInstance().get(Calendar.YEAR) + " Team XBMC\n" +
			" *      http://xbmc.org\n" +
			" *\n" +
			" *  This Program is free software; you can redistribute it and/or modify\n" +
			" *  it under the terms of the GNU General Public License as published by\n" +
			" *  the Free Software Foundation; either version 2, or (at your option)\n" +
			" *  any later version.\n" +
			" *\n" +
			" *  This Program is distributed in the hope that it will be useful,\n" +
			" *  but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
			" *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n" +
			" *  GNU General Public License for more details.\n" +
			" *\n" +
			" *  You should have received a copy of the GNU General Public License\n" +
			" *  along with XBMC Remote; see the file license.  If not, write to\n" +
			" *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.\n" +
			" *  http://www.gnu.org/copyleft/gpl.html\n" +
			" *\n" +
			" */\n";

}
