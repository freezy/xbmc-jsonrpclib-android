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

import org.xbmc.android.jsonrpc.generator.model.JavaAttribute;
import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaConstructor;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.ConstructorView;
import org.xbmc.android.jsonrpc.generator.view.MemberView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;

/**
 * Declares all members in the class as class members.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class MemberDeclarationClassModule extends AbstractView implements IClassModule {

	
	@Override
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		final String indent = getIndent(idt);

		// field name constants
		if (!klass.isMultiType()) {
			sb.append("\n").append(indent).append("// field names\n");
			for (JavaAttribute member : klass.getMembers()) {
				final MemberView memberView = new MemberView(member);
				sb.append(memberView.renderFieldDeclaration(idt));
			}
		}

		// members
		sb.append("\n").append(indent).append("// class members\n");
		for (JavaAttribute member : klass.getMembers()) {
			final MemberView memberView = new MemberView(member);
			if (!klass.equals(member.getParent())) {
				sb.append(indent).append("/**\n");
				sb.append(indent).append(" * Multiple inheritage: copied from <tt>");
				sb.append(member.getParent().getApiType());
				sb.append("</tt>.\n");
				sb.append(indent).append(" */\n");
			}
			memberView.renderDeclaration(sb, klass.getNamespace(), idt);
		}
		
		// constructors
		if (klass.isUsedAsParameter() || !klass.doesExtend()) {
			for (JavaConstructor c : klass.getConstructors()) {
				final ConstructorView constructorView = new ConstructorView(c);
				constructorView.renderDeclaration(sb, klass.getNamespace(), idt);
			}
		}
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		return new HashSet<String>();
	}
	
}
