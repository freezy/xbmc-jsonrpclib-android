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

import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaMember;
import org.xbmc.android.jsonrpc.generator.model.JavaParameter;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Renders a Java class constructor.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class ConstructorView extends AbstractView {

	private final JavaConstructor constructor;

	public ConstructorView(JavaConstructor constructor) {
		this.constructor = constructor;
	}

	public void renderDeclaration(StringBuilder sb, Namespace ns, int idt) {
		final String indent = getIndent(idt);
		
		// parent member list
		final StringBuilder parentMemberDeclaration = new StringBuilder();
		final StringBuilder parentMemberList = new StringBuilder();
		if (constructor.getType().doesExtend()) {
			for (JavaMember m : constructor.getType().getParentMembers()) {
				if (m.isEnum()) {
					parentMemberDeclaration.append(getClassName(ns, m));
				} else {
					parentMemberDeclaration.append(getClassReference(ns, m.getType()));
				}
				parentMemberDeclaration.append(" ");
				parentMemberDeclaration.append(m.getName());
				parentMemberDeclaration.append(", ");
				
				parentMemberList.append(m.getName());
				parentMemberList.append(", ");
			}
			parentMemberDeclaration.delete(parentMemberDeclaration.length() - 2, parentMemberDeclaration.length());
			parentMemberList.delete(parentMemberList.length() - 2, parentMemberList.length());
		}
		
		
		// header
		sb.append("\n");
		sb.append(indent).append("/**\n");
		if (constructor.getType().hasDescription()) {
			sb.append(getDescription(constructor.getType(), indent));
		}
		if (constructor.hasParameters()) {
			for (JavaParameter p : constructor.getParameters()) {
				sb.append(indent).append(" * @param ");
				sb.append(p.getName());
				sb.append(" ");
				if (p.hasDescription()) {
					sb.append(p.getDescription());
				}
				renderEnumComment(sb, ns, p);
				sb.append("\n");
				if (!p.isEnum()) {
					sb.append(getDescription(p.getType()));
				}
			}
			sb.append(indent).append(" */\n");
		}
		
		// signature
		sb.append(indent).append("public ");
		sb.append(getClassName(constructor.getType()));
		sb.append("(");
		if (constructor.getType().doesExtend()) {
			sb.append(parentMemberDeclaration.toString());
			sb.append(", ");
		}
		if (constructor.hasParameters()) {
			for (JavaParameter p : constructor.getParameters()) {
				if (p.isEnum()) {
					sb.append(getClassName(ns, p));
				} else {
					sb.append(getClassReference(ns, p.getType()));
				}
				sb.append(" ");
				sb.append(p.getName());
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(") {\n");
		
		// body
		String lastArg = null;
		if (constructor.getType().doesExtend()) {
			sb.append(indent).append("	super(");
			sb.append(parentMemberList.toString());
			sb.append(");\n");
		}
		for (JavaParameter p : constructor.getParameters()) {
			sb.append(indent).append("\tthis.");
			sb.append(p.getName());
			sb.append(" = ");
			sb.append(p.getName());
			sb.append(";\n");
			lastArg = p.getName();
		}
		
		// if multi type, init non-used vars as null
		if (constructor.getType().isMultiType()) {
			for (JavaMember member : constructor.getType().getMembers()) {
				// all but the one we already have.
				if (lastArg != null && lastArg.equals(member.getName())) {
					continue;
				}
				sb.append(indent).append("\tthis.");
				sb.append(member.getName());
				sb.append(" = null;\n");
			}
		}
		
		sb.append(indent).append("}\n");
	}
}
