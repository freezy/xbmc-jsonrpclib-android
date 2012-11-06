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
package org.xbmc.android.jsonrpc.generator.view.module.classmodule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaMember;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;

/**
 * Provides JSON-serialization via the Jackson library.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class JsonAccesClassModule extends AbstractView implements IClassModule {

	public static final Map<String, String> NATIVE_REQUIRED_NODE_GETTER = new HashMap<String, String>();
	public static final Map<String, String> NATIVE_OPTIONAL_NODE_GETTER = new HashMap<String, String>();
	public static final Map<String, String> NATIVE_NODE_TYPECHECK = new HashMap<String, String>();
	
	static {
		NATIVE_REQUIRED_NODE_GETTER.put("integer", "getIntValue");
		NATIVE_REQUIRED_NODE_GETTER.put("string", "getTextValue");
		NATIVE_REQUIRED_NODE_GETTER.put("boolean", "getBooleanValue");
		NATIVE_REQUIRED_NODE_GETTER.put("number", "getDoubleValue");
		NATIVE_REQUIRED_NODE_GETTER.put("any", "getTextValue");

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
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {

		// 1. render class constructor
		renderNodeConstructor(sb, ns, klass, idt);
		
		// toObjectNode()
		renderToObjectNode(sb, klass, ns, idt);
		
		// render list getter
		renderListGetter(sb, klass, ns, idt);
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("org.codehaus.jackson.node.ArrayNode");
		imports.add("org.codehaus.jackson.node.ObjectNode");
		imports.add("java.util.List");
		imports.add("java.util.ArrayList");
		return imports;
	}
	
	/**
	 * Renders the entire constructor taking in one ObjectNode which is
	 * then parsed into the class member values.
	 * @param sb
	 * @param idt
	 * @param klass
	 */
	private void renderNodeConstructor(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		
		final String indent = getIndent(idt);
		
		// comment
		sb.append("\n");
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Construct from JSON object.\n");
		sb.append(indent).append(" * @param node JSON object representing a ");
		sb.append(getClassName(klass));
		sb.append(" object\n");
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("public ");
		sb.append(getClassName(klass));
		sb.append("(ObjectNode node) {\n");
		
		// call to super()
		if (klass.doesExtend()) {
			sb.append(indent).append("	super(node);\n");
		}
		
		// parse members
//		sb.append(indent).append("	mType = API_TYPE;\n");
		boolean isFirst = true;
		for (JavaMember member : klass.getMembers()) {
			if (klass.isMultiType()) {
				renderMultiTypeLine(sb, member, klass.getMembers(), idt + 1, isFirst);
			} else {
				sb.append(indent).append("\t");
				sb.append(member.getName());
				sb.append(" = ");
				renderParseLine(sb, member, ns);
			}
			isFirst = false;
		}
		if (klass.isMultiType()) {
			sb.append(indent).append("	else {\n");
			sb.append(indent).append("		throw new RuntimeException(\"Weird type for \\\"");
			sb.append(klass.getName());
			sb.append("\\\", I'm confused!\");\n");
			sb.append(indent).append("	}\n");
		}

		sb.append(indent).append("}\n");
	}
	
	private void renderToObjectNode(StringBuilder sb, JavaClass klass, Namespace ns, int idt) {
		final String indent = getIndent(idt);
		
		// comment
		sb.append("\n");
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public ObjectNode toObjectNode() {\n");
		
		if (!klass.isMultiType()) {
			
			sb.append(indent).append("	final ObjectNode node = OM.createObjectNode();\n");
			for (JavaMember member : klass.getMembers()) {
				renderPutLine(sb, member, ns, idt + 1);
			}
			sb.append(indent).append("	return node;\n");
			
		} else {
			// TODO return JsonBaseNode or whatever
			sb.append(indent).append("	return null; // return JsonBaseNode or whatever\n");
		}
		
		sb.append(indent).append("}\n");
	}
	
	private void renderPutLine(StringBuilder sb, JavaMember member, Namespace ns, int idt) {
		final String indent = getIndent(idt);
		
		if (member.isEnum()) {
			sb.append(indent);
			renderNodeSetter(sb, member, member.getName());
			sb.append("; // enum\n");
		} else {
			final JavaClass klass = member.getType();
			if (klass.isNative()) {
				
				sb.append(indent);
				renderNodeSetter(sb, member, member.getName());
				sb.append(";\n");
				
			} else if (klass.isTypeArray()) {
				final String arrayName = member.getName() + "Array";
				
				// like: final ArrayNode dependencyArray = OM.createArrayNode();
				sb.append(indent).append("final ArrayNode ");
				sb.append(arrayName);
				sb.append(" = OM.createArrayNode();\n");
				
				// like: for (Dependencies item : dependencies) {
				sb.append(indent).append("for (");
				sb.append(getClassReference(ns, klass.getArrayType()));
				sb.append(" item : ");
				sb.append(member.getName());
				sb.append(") {\n");
				
				// like: dependenciesArray.add(item.toObjectNode());
				sb.append(indent).append("\t");
				sb.append(arrayName);
				sb.append(".add(");
				if (klass.getArrayType().isNative()) {
					sb.append("item");
				} else {
					sb.append("item.toObjectNode()");
				}
				sb.append(");\n");
				
				sb.append(indent).append("}\n");
				sb.append(indent);
				renderNodeSetter(sb, member, arrayName);
				sb.append(";\n");
				
			} else {
				// like: node.put(BROKEN, broken.toObjectNode());
				sb.append(indent);
				renderNodeSetter(sb, member, member.getName() + ".toObjectNode()");
				sb.append(";\n");
			}
		}
	}
	
	
	/**
	 * Within the constructor, this writes the value of the field depending on 
	 * the class type.
	 * 
	 * @param sb
	 * @param member
	 */
	private void renderParseLine(StringBuilder sb, JavaMember member, Namespace ns) {
		if (member.isEnum()) {
			renderOptionalNativeNodeGetter(sb, member.getName(), NATIVE_OPTIONAL_NODE_GETTER.get("string"));
		} else {
			final JavaClass klass = member.getType();
			
			if (klass.isNative() && member.isRequired()) {
				renderRequiredNativeNodeGetter(sb, member.getName(), NATIVE_REQUIRED_NODE_GETTER.get(klass.getName()));
				
			} else if (klass.isNative()) {
				renderOptionalNativeNodeGetter(sb, member.getName(), NATIVE_OPTIONAL_NODE_GETTER.get(klass.getName()));
				
			} else if (klass.isTypeArray()) { // native arrays
				final JavaClass arrayType = klass.getArrayType();
				if (arrayType.isNative()) {
					
					// like: getStringArray(node, MOOD);
					if (arrayType.getName().equals("string")) {
						renderOptionalNativeNodeGetter(sb, member.getName(), "getStringArray");
					}
					if (arrayType.getName().equals("integer")) {
						renderOptionalNativeNodeGetter(sb, member.getName(), "getIntegerArray");
					}
				} else {
					// like: Dependency.getDependencyList(node, DEPENDENCIES);
					sb.append(getClassReference(ns, arrayType));
					sb.append(".");
					sb.append(getListGetter(arrayType));
					sb.append("(node, ");
					sb.append(member.getName().toUpperCase());
					sb.append(");\n");
				}
			} else {
				if (member.isRequired()) {
					// like: new Broken((ObjectNode)node.get(BROKEN));
					renderObjectNodeGetter(sb, ns, klass, member.getName());
					sb.append(";\n");
					
				} else {
					// like: node.has(BROKEN) ? new Broken((ObjectNode)node.get(BROKEN)) : null;
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
	 * @param idt
	 * @param isFirst
	 */
	private void renderMultiTypeLine(StringBuilder sb, JavaMember member, List<JavaMember> allMembers, int idt, boolean isFirst) {
		final String indent = getIndent(idt);
		if (!member.isEnum()) {
			final JavaClass klass = member.getType();
			
			sb.append(indent);
			if (!isFirst) {
				sb.append("else ");
			}
			
			// native types
			if (klass.isNative()) {
				
				if (!NATIVE_NODE_TYPECHECK.containsKey(klass.getName())) {
					throw new RuntimeException("Unknown native type \"" + klass.getName() + "\".");
				}
				
				sb.append("if (node.");
				sb.append(NATIVE_NODE_TYPECHECK.get(klass.getName()));
				sb.append("()) {\n");
				sb.append(indent).append("\t");
				sb.append(member.getName());
				sb.append(" = node.");
				sb.append(NATIVE_REQUIRED_NODE_GETTER.get(klass.getName()));
				sb.append("();\n");
				for (JavaMember m : allMembers) {
					if (m != member) {
						sb.append(indent).append("\t");
						sb.append(m.getName());
						sb.append(" = null;\n");
					}
				}
			} else {
				// TODO check what's returned and see if we can match by name rather than type
				sb.append("if (node.isObject()) { // check what's returned and see if we can match by name rather than type.\n");
				for (JavaMember m : allMembers) {
					sb.append(indent).append("\t");
					sb.append(m.getName());
					sb.append(" = null;\n");
				}
			}
			sb.append(indent).append("}\n");
		}
	}
	
	private void renderListGetter(StringBuilder sb, JavaClass klass, Namespace ns, int idt) {
		final String indent = getIndent(idt);
		final String name = getClassReference(ns, klass);

		// method header comment
		sb.append("\n");
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Extracts a list of {@link ");
		sb.append(name);
		sb.append("} objects from a JSON array.\n");
		sb.append(indent).append(" * @param obj ObjectNode containing the list of objects.\n");
		sb.append(indent).append(" * @param key Key pointing to the node where the list is stored.\n");
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("static List<");
		sb.append(name);
		sb.append("> ");
		sb.append(getListGetter(klass));
		sb.append("(ObjectNode node, String key) {\n");
		
		sb.append(indent).append("	if (node.has(key)) {\n");
		sb.append(indent).append("		final ArrayNode a = (ArrayNode)node.get(key);\n");
		sb.append(indent).append("		final List<").append(name).append("> l = new ArrayList<").append(name).append(">(a.size());\n");
		sb.append(indent).append("		for (int i = 0; i < a.size(); i++) {\n");
		sb.append(indent).append("			l.add(new ").append(name).append("((ObjectNode)a.get(i)));\n");
		sb.append(indent).append("		}\n");
		sb.append(indent).append("		return l;\n");
		sb.append(indent).append("	}\n");
		sb.append(indent).append("	return new ArrayList<").append(name).append(">(0);\n");
		
		sb.append(indent).append("}\n");
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
	private void renderObjectNodeGetter(StringBuilder sb, Namespace ns, JavaClass klass, String name) {
		sb.append("new ");
		sb.append(getClassReference(ns, klass));
		sb.append("((ObjectNode)node.get(");
		sb.append(name.toUpperCase());
		sb.append("))");
	}
	
	/**
	 * Returns something like <tt>node.put(AUTHOR, author)</tt>.
	 * Note there is no semicolon nor newline for this.
	 * @param sb
	 * @param member
	 */
	private void renderNodeSetter(StringBuilder sb, JavaMember member, String value) {
		sb.append("node.put(");
		sb.append(member.getName().toUpperCase());
		sb.append(", ");
		sb.append(value);
		sb.append(")");
	}
	
}
