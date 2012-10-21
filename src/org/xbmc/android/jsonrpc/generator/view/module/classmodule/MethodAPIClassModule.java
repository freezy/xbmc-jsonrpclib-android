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
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaMember;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;


/**
 * Provides Parcelable-serialization via Android.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MethodAPIClassModule extends AbstractView implements IClassModule {

	
	@Override
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		
		renderStaticStuff(sb, klass, idt);
		
	}
	
	
	private void renderStaticStuff(StringBuilder sb, JavaClass klass, int idt) {
		final String indent = getIndent(idt);
		
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public String getName() {\n");
		sb.append(indent).append("	return API_TYPE;\n");
		sb.append(indent).append("}\n");
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("protected boolean returnsList() {\n");
		sb.append(indent).append("	return false;\n");
//		sb.append(indent).append("	return ").append(klass.).append(";\n");
		sb.append(indent).append("}");
	}
	
	

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("android.os.Parcel");
		imports.add("android.os.Parcelable");
		imports.addAll(getInternalImports(klass));
		
		for (JavaClass innerClass : klass.getInnerTypes()) {
			imports.addAll(getInternalImports(innerClass));
		}
		
		return imports;
	}
	
	public Set<String> getInternalImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		for (JavaMember member : klass.getMembers()) {
			if (!member.isEnum()) {
				final JavaClass memberType = member.getType();
				if (memberType.isGlobal()) {
					imports.add(memberType.getNamespace().getPackageName() + "." + memberType.getNamespace().getName());
				}
			}
		}
		return imports;
	}
	
}
