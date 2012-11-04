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

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.JavaEnum;
import org.xbmc.android.jsonrpc.generator.model.JavaMember;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.JavaParameter;
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
		
		
		renderStaticStuff(sb, method, idt);
	}
	
	private void renderEnumValues(StringBuilder sb, JavaEnum e) {
		for (String value : e.getValues()) {
			sb.append("<tt>");
			sb.append(value);
			sb.append("</tt>, ");
		}
		if (!e.getValues().isEmpty()) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(".");
		
	}
	
	private void renderConstructor(StringBuilder sb, Namespace ns, JavaMethod method, JavaConstructor constructor, int idt) {
		final String indent = getIndent(idt);
		
		// header
		sb.append(indent).append("/**\n");
		if (method.hasDescription()) {
			sb.append(getDescription(method, indent));
		}
		for (JavaParameter p : constructor.getParameters()) {
			sb.append(indent).append(" * @param ");
			sb.append(p.getName());
			if (p.hasDescription()) {
				sb.append(p.getDescription());
			}
			if (p.isEnum()) {
				if (p.getEnum().isArray()) {
					sb.append(" One or more of: ");
				} else {
					sb.append(" One of: ");
				}
				renderEnumValues(sb, p.getEnum());
				sb.append(" See constants at {@link ").append(getEnumReference(ns, p.getEnum())).append("}.");
				
			} else if (p.getType().isEnumArray()) {
				sb.append(" One or more of: ");
				renderEnumValues(sb, p.getType().getEnumArray());
				sb.append(" See constants at {@link ").append(getEnumReference(ns, p.getType().getEnumArray())).append("}.");
			}
			sb.append("\n");
		}
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("public ");
		sb.append(getClassName(method));
		sb.append("(");
		Iterator<JavaParameter> it = constructor.getParameters().iterator();
		while (it.hasNext()) {
			final JavaParameter p = it.next();
			if (!it.hasNext() && p.isArray()) {
				// if enum != null, we know it's an enum array, otherwise isArray() woulnd't have returned true.
				// in all other cases, p.getType() != null.
				if (p.getEnum() != null || p.getType().isEnumArray()) {
					sb.append("String...");
				} else {
					sb.append(getClassReference(ns, p.getType().getArrayType()));
					sb.append("...");
				}
			} else {
				if (p.isEnum()) {
					sb.append("String");
				} else {
					sb.append(getClassName(p.getType(), true));
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
		for (JavaParameter p : constructor.getParameters()) {
			sb.append(indent).append("	addParameter(\"");
			sb.append(p.getName());
			sb.append("\", ");
			sb.append(p.getName());
			sb.append(");\n");
		}
		
		
		sb.append(indent).append("}\n");
		
	}
	
	private void renderStaticStuff(StringBuilder sb, JavaMethod method, int idt) {
		final String indent = getIndent(idt);
		
		// public String getName() { }
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public String getName() {\n");
		sb.append(indent).append("	return API_TYPE;\n");
		sb.append(indent).append("}\n");
		
		// protected boolean returnsList() { }
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("protected boolean returnsList() {\n");
		sb.append(indent).append("	return ").append(method.getReturnType().isTypeArray() ? "true" : "false").append(";\n");
		sb.append(indent).append("}\n");
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
//		imports.add("android.os.Parcel");
//		imports.add("android.os.Parcelable");
		imports.addAll(getInternalImports(klass));
		
		for (JavaClass innerClass : klass.getInnerTypes()) {
			imports.addAll(getInternalImports(innerClass));
		}
		
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
		for (JavaMember member : klass.getMembers()) {
			if (!member.isEnum()) {
				final JavaClass memberType = member.getType();
				if (memberType.isGlobal() && memberType.isVisible()) {
					imports.add(memberType.getNamespace().getPackageName() + "." + memberType.getNamespace().getName());
				}
			}
		}
		
		// params in constructors
		for (JavaConstructor constructor : klass.getConstructors()) {
			for (JavaParameter param : constructor.getParameters()) {
				if (!param.isEnum()) {
					final JavaClass paramType = param.getType();
					if (paramType.isGlobal() && !paramType.isNative() && paramType.hasName()) {
						imports.add(
								paramType.getNamespace().getPackageName() 
								+ "." + paramType.getNamespace().getName()
								+ "." + paramType.getName());
					}
				}
			}
		}
		
		return imports;
	}
	
}
