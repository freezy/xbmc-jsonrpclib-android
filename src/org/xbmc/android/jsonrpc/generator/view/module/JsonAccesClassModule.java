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

import java.util.HashSet;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;

/**
 * Provides JSON-serialization via the Jackson library.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JsonAccesClassModule extends AbstractView implements IClassModule {

	@Override
	public void render(StringBuilder sb, int indent, Klass klass) {
		
		renderNodeConstructor(sb, indent, klass);
	}

	@Override
	public Set<String> getImports(Klass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("org.codehaus.jackson.node.ObjectNode");
		return imports;
	}
	
	private void renderNodeConstructor(StringBuilder sb, int indent, Klass klass) {
		
		final String prefix = getIndent(indent);
		
		// comment
		sb.append(prefix).append("/**\n");
		sb.append(prefix).append(" * Construct from JSON object.\n");
		sb.append(prefix).append(" * @param node JSON object representing a ");
		sb.append(getClassName(klass));
		sb.append(" * object\n");
		sb.append(prefix).append(" */\n");
		
		// signature
		sb.append(prefix).append("public ");
		sb.append(getClassName(klass));
		sb.append("(ObjectNode node) {\n");
		
		// parse members
//		sb.append(prefix).append("	mType = API_TYPE;\n");
		for (Member member : klass.getMembers()) {
			sb.append(prefix).append("\t");
			sb.append(member.getName());
			sb.append(" = ");
			renderParseLine(sb, member);
		}

		sb.append(prefix).append("}\n");
	}
	
	private void renderParseLine(StringBuilder sb, Member member) {
		if (member.isEnum()) {
			
		} else {
			final Klass klass = member.getType();
			if (klass.isNative()) {
				if (klass.getName().equals("integer")) {
					sb.append("parseInteger");
				}
			}
			sb.append("(node, ");
			sb.append(member.getName().toUpperCase());
			sb.append(");\n");
		}
	}
}
