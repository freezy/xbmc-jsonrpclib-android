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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaAttribute;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;

/**
 * Provides Parcelable-serialization via Android.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MethodAPIClassModule extends AbstractView implements IClassModule {

	public final static String RESULT_PROPERTY_NAME = "RESULT";
	
	@Override
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		if (!(klass instanceof JavaMethod)) {
			throw new IllegalArgumentException("When rendering method API class modules, passed class must be of type JavaMethod.");
		}
		final String indent = getIndent(idt);
		final JavaMethod method = (JavaMethod)klass;
		
		// property name
		if (method.hasReturnProperty()) {
			sb.append(indent).append("public final static String ");
			sb.append(RESULT_PROPERTY_NAME);
			sb.append(" = \"");
			sb.append(method.getReturnProperty());
			sb.append("\";\n");
		}
		
		for (JavaConstructor jc : method.getConstructors()) {
			renderConstructor(sb, ns, method, jc, idt);
		}
		
		renderParseOneMany(sb, ns, method, idt);
		
		renderStaticStuff(sb, method, idt);
	}
	
	private void renderConstructor(StringBuilder sb, Namespace ns, JavaMethod method, JavaConstructor constructor, int idt) {
		final String indent = getIndent(idt);
		
		// header
		sb.append("\n");
		sb.append(indent).append("/**\n");
		if (method.hasDescription()) {
			sb.append(indent).append(" * ").append(getDescription(method)).append("\n");
		}
		for (JavaAttribute p : constructor.getParameters()) {
			sb.append(indent).append(" * @param ");
			sb.append(p.getName());
			sb.append(getDescription(ns, p));
			sb.append("\n");
		}
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("public ");
		sb.append(getClassName(method));
		sb.append("(");
		Iterator<JavaAttribute> it = constructor.getParameters().iterator();
		while (it.hasNext()) {
			final JavaAttribute p = it.next();
			if (!it.hasNext() && p.isArray()) {
				// if enum != null, we know it's an enum array, otherwise isArray() wouldn't have returned true.
				// in all other cases, p.getType() != null.
				if (p.getEnum() != null) {
					sb.append(p.getEnum().getTypeName());
				} else if (p.getType().isEnumArray()) {
					sb.append("String");
				} else {
					sb.append(getClassReference(ns, p.getType().getArrayType()));
				}
				sb.append("...");
			} else {
				if (p.isEnum()) {
					sb.append(p.getEnum().getTypeName());
				} else {
					sb.append(getClassReference(ns, p.getType(), true));
				}
			}
			sb.append(" ");
			sb.append(p.getName());
			sb.append(", ");
		}
		if (!constructor.getParameters().isEmpty()) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(") {\n");
		
		// body
		sb.append(indent).append("	super();\n");
		for (JavaAttribute p : constructor.getParameters()) {
			sb.append(indent).append("	addParameter(\"");
			sb.append(p.getName());
			sb.append("\", ");
			sb.append(p.getName());
			sb.append(");\n");
		}
		
		
		sb.append(indent).append("}\n");
		
	}
	
	/**
	 * Renders the <tt>parseMany()</tt> or <tt>parseOne()</tt> depending on the
	 * return type.
	 * 
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param method Method
	 * @param idt Indent
	 */
	private void renderParseOneMany(StringBuilder sb, Namespace ns, JavaMethod method, int idt) {
		final String indent = getIndent(idt);
		
		sb.append("\n");
		sb.append(indent).append("@Override\n");
		if (method.getReturnType().isTypeArray()) {
			final String returnType = getClassReference(ns, method.getReturnType().getArrayType());
			final String returnProp = method.getReturnProperty() != null ? method.getReturnProperty() : "results";
			sb.append(indent).append("protected ArrayList<").append(returnType).append("> parseMany(JsonNode node) {\n");
			if (method.hasReturnProperty()) {
				sb.append(indent).append("	final ArrayNode ").append(returnProp).append(" = parseResults(node, RESULT);\n");
			} else {
				sb.append(indent).append("	final ArrayNode ").append(returnProp).append(" = (ArrayNode) node;\n");
			}
			sb.append(indent).append("	if (").append(returnProp).append(" != null) {\n");
			sb.append(indent).append("		final ArrayList<").append(returnType).append("> ret = new ArrayList<").append(returnType).append(">(").append(returnProp).append(".size());\n");
			sb.append(indent).append("		for (int i = 0; i < ").append(returnProp).append(".size(); i++) {\n");
			sb.append(indent).append("			final ObjectNode item = (ObjectNode)").append(returnProp).append(".get(i);\n");
			sb.append(indent).append("			ret.add(new ").append(returnType).append("(item));\n");
			sb.append(indent).append("		}\n");
			sb.append(indent).append("		return ret;\n");
			sb.append(indent).append("	} else {\n");
			sb.append(indent).append("		return new ArrayList<").append(returnType).append(">(0);\n");
			sb.append(indent).append("	}\n");
			sb.append(indent).append("}\n");
			
		} else {
			final String returnType = getClassReference(ns, method.getReturnType());
			sb.append(indent).append("protected ").append(returnType).append(" parseOne(JsonNode node) {\n");
			sb.append(indent).append("\t");
			if (method.getReturnType().isNative()) {
				if (!JsonAccesClassModule.NATIVE_REQUIRED_NODE_GETTER.containsKey(method.getReturnType().getName())) {
					throw new IllegalArgumentException("Unknown return type " + method.getReturnType().getName());
				}
				sb.append("return node.");
				sb.append(JsonAccesClassModule.NATIVE_REQUIRED_NODE_GETTER.get(method.getReturnType().getName()));
				sb.append("();\n");
				
			} else {
				if (method.hasReturnProperty()) {
					sb.append("return new ").append(returnType).append("((ObjectNode)node.get(RESULT));\n");
				} else {
					sb.append("return new ").append(returnType).append("(node);\n");
				}
				
			}
			sb.append(indent).append("}\n");
		}
	}
	
	/**
	 * Renders <tt>getName()</tt> and <tt>returnsList()</tt>.
	 * 
	 * @param sb Current StringBuilder
	 * @param method Method
	 * @param idt Indent
	 */
	private void renderStaticStuff(StringBuilder sb, JavaMethod method, int idt) {
		final String indent = getIndent(idt);
		
		// public String getName() { }
		sb.append("\n");
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public String getName() {\n");
		sb.append(indent).append("	return API_TYPE;\n");
		sb.append(indent).append("}\n");
		
		// protected boolean returnsList() { }
		sb.append("\n");
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("protected boolean returnsList() {\n");
		sb.append(indent).append("	return ").append(method.getReturnType().isTypeArray() ? "true" : "false").append(";\n");
		sb.append(indent).append("}\n");
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.addAll(getInternalImports(klass));
		
		for (JavaClass innerClass : klass.getInnerTypes()) {
			imports.addAll(getInternalImports(innerClass));
		}
		
		if (((JavaMethod)klass).getReturnType().isTypeArray()) {
			imports.add("java.util.ArrayList");
			imports.add("org.codehaus.jackson.node.ArrayNode");
		}
		imports.add("org.codehaus.jackson.node.ObjectNode");
		imports.add("org.codehaus.jackson.JsonNode");
		
		return imports;
	}
	
	/**
	 * Computes imports that refer to the API's global types.
	 * @param klass
	 * @return
	 */
	private Set<String> getInternalImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		
		// members
		for (JavaAttribute member : klass.getMembers()) {
			if (!member.isEnum()) {
				final JavaClass memberType = member.getType();
				if (memberType.isGlobal() && memberType.isVisible()) {
					imports.add(memberType.getNamespace().getPackageName() + "." + memberType.getNamespace().getName());
				}
			}
		}
		
		// params in constructors
		for (JavaConstructor constructor : klass.getConstructors()) {
			for (JavaAttribute param : constructor.getParameters()) {
				if (!param.isEnum()) {
					final JavaClass paramType = param.getType();
					if (paramType.isGlobal() && !paramType.isNative() && paramType.hasName()) {
						imports.add(
								paramType.getNamespace().getPackageName() 
								+ "." + paramType.getNamespace().getName());
					}
				}
				if (param.isMap()) {
					imports.add("java.util.HashMap");
				}
			}
		}
		
		return imports;
	}
	
}
