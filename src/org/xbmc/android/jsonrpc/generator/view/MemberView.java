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

import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

/**
 * Renders a Java class member.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MemberView extends AbstractView {
	
	private final Member member;
	
	public MemberView(Member member) {
		this.member = member.resolveType();
	}
	
	public void renderDeclaration(StringBuilder sb, Namespace ns, int indent) {
		
		final String prefix = getIndent(indent);
		
		sb.append(prefix).append("public final ");
		if (member.isEnum()) {
			sb.append(getClassName(ns, member));
		} else {
			sb.append(getClassReference(ns, member.getType()));
		}
		sb.append(" ").append(member.getName());
		sb.append(";\n");
	}
	
	public String renderFieldDeclaration(int indent) {
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("public static final String ");
		sb.append(member.getName().toUpperCase());
		sb.append(" = \"");
		sb.append(member.getName());
		sb.append("\";\n");
		return sb.toString();
	}
	
}
