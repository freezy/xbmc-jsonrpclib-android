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
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;

/**
 * Provides Parcelable-serialization via Android.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class ParcelableClassModule extends AbstractView implements IClassModule {

	
	@Override
	public void render(StringBuilder sb, Namespace ns, Klass klass, int idt) {
		
		// writeToParcel
		renderWriteToParcel(sb, ns, klass, idt);
		
		// describeContents
		renderDescribeContents(sb, idt);
	}

	@Override
	public Set<String> getImports(Klass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("android.os.Parcel");
//		imports.add("android.os.Parcelable");
		return imports;
	}
	
	private void renderWriteToParcel(StringBuilder sb, Namespace ns, Klass klass, int idt) {
		final String indent = getIndent(idt);
		
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public void writeToParcel(Parcel parcel, int flags) {\n");
		if (klass.doesExtend()) {
			sb.append(indent).append("	super.writeToParcel(parcel, flags);\n");
		}
		for (Member member : klass.getMembers()) {
			renderWriteToParcel(sb, ns, member, idt + 1);
		}
		sb.append(indent).append("}\n");
	}
	
	private void renderWriteToParcel(StringBuilder sb, Namespace ns, Member member, int idt) {
		final String indent = getIndent(idt);
		
		if (member.isEnum()) {
			// TODO
			sb.append(indent).append("/* enum: ").append(member.getName()).append(" */\n");
		} else {
			
			final Klass klass = member.getType();
			if (klass.isArray()) {
				final Klass arrayType = klass.getArrayType();
				
				// like: parcel.writeInt(genre.size());
				sb.append(indent).append("parcel.writeInt(");
				sb.append(member.getName());
				sb.append(".size());\n");
				
				// like: for (String item : genre) {
				sb.append(indent).append("for (");
				sb.append(getClassReference(ns, arrayType));
				sb.append(" item : ");
				sb.append(member.getName());
				sb.append(") {\n");
				
				// like: parcel.writeValue(item);
				sb.append(indent).append("	parcel.");
				if (arrayType.isNative()) {
					sb.append("writeValue(item);\n");
				} else {
					sb.append("writeParcelable(item, flags);\n");
				}
				
				sb.append(indent).append("}\n");
				
			} else {
				// like: parcel.writeInt(muted ? 1 : 0);
				if (klass.isNative() && klass.getName().equals("boolean")) {
					sb.append(indent).append("parcel.writeInt(");
					sb.append(member.getName());
					sb.append(" ? 1 : 0);\n");
				
				// like: parcel.writeValue(data);
				} else {
					sb.append(indent).append("parcel.writeValue(");
					sb.append(member.getName());
					sb.append(");\n");
				}
			}
			
		}
	}
	
	
	private void renderDescribeContents(StringBuilder sb, int idt) {
		final String indent = getIndent(idt);
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public int describeContents() {\n");
		sb.append(indent).append("	return 0;\n");
		sb.append(indent).append("}\n");
	}
	
}
