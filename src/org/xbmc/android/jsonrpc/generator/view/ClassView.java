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
import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;

/**
 * Renders a Java class.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class ClassView extends AbstractView {

	//public final static String DISPLAY_ONLY = "Filter.Albums";
	public final static String DISPLAY_ONLY = "";

	private final Klass klass;

	public ClassView(Klass klass) {
		this.klass = klass;
	}

	public void renderDeclaration(StringBuilder sb, int indent, boolean force) {

		// debug
		if (!force && !DISPLAY_ONLY.isEmpty() && !klass.getApiType().equals(DISPLAY_ONLY)) {
			return;
		}

		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}

		sb.append("\n");
		sb.append(prefix).append("public static class ");
		sb.append(getClassName(klass));
		sb.append(" {\n");

		// field names
		if (!klass.isMultiType()) {
			sb.append("\n").append(prefix).append("\t// field names\n");
			for (Member member : klass.getMembers()) {
				final MemberView memberView = new MemberView(member);
				sb.append(memberView.renderFieldDeclaration(indent + 1));
			}
		}

		// members
		sb.append("\n").append(prefix).append("\t// class members\n");
		for (Member member : klass.getMembers()) {
			final MemberView memberView = new MemberView(member);
			memberView.renderDeclaration(sb, indent + 1);
		}
		
		// constructors
		for (Constructor c : klass.getConstructors()) {
			final ConstructorView constructorView = new ConstructorView(c);
			constructorView.renderDeclaration(sb, indent + 1);
		}

		// inner classes
		if (klass.hasInnerTypes()) {
			for (Klass innerClass : klass.getInnerTypes()) {
				final ClassView classView = new ClassView(innerClass);
				classView.renderDeclaration(sb, indent + 1, true);
			}
		}

		// inner enums
		if (klass.hasInnerEnums()) {
			for (Enum e : klass.getInnerEnums()) {
				final EnumView enumView = new EnumView(e);
				enumView.render(sb, indent + 1, true);
			}
		}
		
		sb.append(prefix).append("}\n");
	}


}
