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
import org.xbmc.android.jsonrpc.generator.model.Namespace;
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
	public void render(StringBuilder sb, Namespace ns, Klass klass, int indent) {

		// 1. render class constructor
		renderNodeConstructor(sb, ns, klass, indent);
	}

	@Override
	public Set<String> getImports(Klass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("org.codehaus.jackson.node.ObjectNode");
		return imports;
	}
	
	/**
	 * Renders the entire constructor taking in one ObjectNode which is
	 * then parsed into the class member values.
	 * @param sb
	 * @param indent
	 * @param klass
	 */
	private void renderNodeConstructor(StringBuilder sb, Namespace ns, Klass klass, int indent) {
		
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
				renderParseLine(sb, member, ns);
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
	
	/**
	 * Within the constructor, this writes the value of the field depending on 
	 * the class type.
	 * 
	 * @param sb
	 * @param member
	 */
	private void renderParseLine(StringBuilder sb, Member member, Namespace ns) {
		if (member.isEnum()) {
			// TODO
			sb.append("null; /* enum */\n");
		} else {
			final Klass klass = member.getType();
			
			if (klass.isNative() && member.isRequired()) {
				renderRequiredNativeNodeGetter(sb, member.getName(), NATIVE_REQUIRED_NODE_GETTER.get(klass.getName()));
				
			} else if (klass.isNative()) {
				renderOptionalNativeNodeGetter(sb, member.getName(), NATIVE_OPTIONAL_NODE_GETTER.get(klass.getName()));
				
			} else if (klass.isArray()) { // native arrays
				final Klass arrayType = klass.getArrayType();
				if (arrayType.isNative()) {
					
					if (arrayType.getName().equals("string")) {
						renderOptionalNativeNodeGetter(sb, member.getName(), "getStringArray");
					}
					if (arrayType.getName().equals("integer")) {
						renderOptionalNativeNodeGetter(sb, member.getName(), "getIntegerArray");
					}
				} else {
					// TODO
					sb.append("null; /* needs JavaArrayCreatorMethod */\n");
				}
			} else {
				if (member.isRequired()) {
					renderObjectNodeGetter(sb, ns, klass, member.getName());
					sb.append(";\n");
				} else {
					sb.append("node.has(");
					sb.append(member.getName().toUpperCase());
					sb.append(") ? ");
					renderObjectNodeGetter(sb, ns, klass, member.getName());
					sb.append(" : null;\n");
				}
			}
		}
	}
	
	/**
	 * For a multitype constructor, this renders the field attribution for one member.
	 * @param sb
	 * @param member
	 * @param allMembers
	 * @param indent
	 * @param isFirst
	 */
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

	
	/**
	 * Returns something like <tt>node.get(ALBUMID).getIntValue();</tt>
	 * @param sb
	 * @param name
	 * @param method
	 */
	private void renderRequiredNativeNodeGetter(StringBuilder sb, String name, String method) {
		sb.append("node.get(");
		sb.append(name.toUpperCase());
		sb.append(").");
		sb.append(method);
		sb.append("(); // required value\n");
	}
	
	/**
	 * Returns something like <tt>parseString(node, ALBUMLABEL);</tt>
	 * @param sb
	 * @param name
	 * @param method
	 */
	private void renderOptionalNativeNodeGetter(StringBuilder sb, String name, String method) {
		sb.append(method);
		sb.append("(node, ");
		sb.append(name.toUpperCase());
		sb.append(");\n");
	}
	
	/**
	 * Returns something like <tt>new Broken((ObjectNode)node.get(BROKEN))</tt>.
	 * Note there is no semicolon nor newline for this.
	 * @param sb
	 * @param name
	 * @param method
	 */
	private void renderObjectNodeGetter(StringBuilder sb, Namespace ns, Klass klass, String name) {
		sb.append("new ");
		sb.append(getClassReference(ns, klass));
		sb.append("((ObjectNode)node.get(");
		sb.append(name.toUpperCase());
		sb.append("))");
	}
}
