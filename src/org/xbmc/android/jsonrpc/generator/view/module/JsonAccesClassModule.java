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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	private static final Map<String, String> NATIVE_REQUIRED_NODE_GETTER = new HashMap<String, String>();
	private static final Map<String, String> NATIVE_OPTIONAL_NODE_GETTER = new HashMap<String, String>();
	private static final Map<String, String> NATIVE_NODE_TYPECHECK = new HashMap<String, String>();
	
	static {
		NATIVE_REQUIRED_NODE_GETTER.put("integer", "getIntValue");
		NATIVE_REQUIRED_NODE_GETTER.put("string", "getTextValue");
		NATIVE_REQUIRED_NODE_GETTER.put("boolean", "getBooleanValue");
		NATIVE_REQUIRED_NODE_GETTER.put("number", "getDoubleValue");

		NATIVE_OPTIONAL_NODE_GETTER.put("integer", "parseInt");
		NATIVE_OPTIONAL_NODE_GETTER.put("string", "parseString");
		NATIVE_OPTIONAL_NODE_GETTER.put("boolean", "parseBoolean");
		NATIVE_OPTIONAL_NODE_GETTER.put("number", "parseDouble");
		
		NATIVE_NODE_TYPECHECK.put("integer", "isInt");
		NATIVE_NODE_TYPECHECK.put("string", "isTextual");
		NATIVE_NODE_TYPECHECK.put("boolean", "isBoolean");
		NATIVE_NODE_TYPECHECK.put("number", "isDouble");
	}
	
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
		sb.append(" object\n");
		sb.append(prefix).append(" */\n");
		
		// signature
		sb.append(prefix).append("public ");
		sb.append(getClassName(klass));
		sb.append("(ObjectNode node) {\n");
		
		// call to super()
		if (klass.doesExtend()) {
			sb.append(prefix).append("	super(node);\n");
		}
		
		// parse members
//		sb.append(prefix).append("	mType = API_TYPE;\n");
		boolean isFirst = true;
		for (Member member : klass.getMembers()) {
			if (klass.isMultiType()) {
				renderMultiTypeLine(sb, member, klass.getMembers(), indent + 1, isFirst);
			} else {
				sb.append(prefix).append("\t");
				sb.append(member.getName());
				sb.append(" = ");
				renderParseLine(sb, member);
			}
			isFirst = false;
		}
		if (klass.isMultiType()) {
			sb.append(prefix).append("	else {\n");
			sb.append(prefix).append("		throw new RuntimeException(\"Weird type for \\\"");
			sb.append(klass.getName());
			sb.append("\\\", I'm confused!\");\n");
			sb.append(prefix).append("	}\n");
		}

		sb.append(prefix).append("}\n");
	}
	
	private void renderMultiTypeLine(StringBuilder sb, Member member, List<Member> allMembers, int indent, boolean isFirst) {
		final String prefix = getIndent(indent);
		if (!member.isEnum()) {
			final Klass klass = member.getType();
			
			// native types
			if (klass.isNative()) {
				
				if (!NATIVE_NODE_TYPECHECK.containsKey(klass.getName())) {
					throw new RuntimeException("Unknown native type \"" + klass.getName() + "\".");
				}
				
				sb.append(prefix);
				if (!isFirst) {
					sb.append("else ");
				}
				sb.append("if (node.");
				sb.append(NATIVE_NODE_TYPECHECK.get(klass.getName()));
				sb.append("()) {\n");
				sb.append(prefix).append("\t");
				sb.append(member.getName());
				sb.append(" = node.");
				sb.append(NATIVE_REQUIRED_NODE_GETTER.get(klass.getName()));
				sb.append("();\n");
				for (Member m : allMembers) {
					if (m != member) {
						sb.append(prefix).append("\t");
						sb.append(m.getName());
						sb.append(" = null;\n");
					}
				}
				sb.append(prefix).append("}\n");
			}
		}
	}
	
	private void renderParseLine(StringBuilder sb, Member member) {
		if (member.isEnum()) {
			sb.append("null;\n");
		} else {
			final Klass klass = member.getType();
			
			// native types
			if (klass.isNative()) {
				
				// required values
				if (member.isRequired()) {
					
					if (!NATIVE_REQUIRED_NODE_GETTER.containsKey(klass.getName())) {
						throw new RuntimeException("Unknown native type \"" + klass.getName() + "\".");
					}
					sb.append("node.get(");
					sb.append(member.getName().toUpperCase());
					sb.append(").");
					sb.append(NATIVE_REQUIRED_NODE_GETTER.get(klass.getName()));
					sb.append("(); // required value\n");
				
				// optional values	
				} else {
					
					if (!NATIVE_OPTIONAL_NODE_GETTER.containsKey(klass.getName())) {
						throw new RuntimeException("Unknown native type \"" + klass.getName() + "\".");
					}
					sb.append(NATIVE_OPTIONAL_NODE_GETTER.get(klass.getName()));
					sb.append("(node, ");
					sb.append(member.getName().toUpperCase());
					sb.append(");\n");
				}
			} else {
				sb.append("null;\n");
			}
		}
	}
}
