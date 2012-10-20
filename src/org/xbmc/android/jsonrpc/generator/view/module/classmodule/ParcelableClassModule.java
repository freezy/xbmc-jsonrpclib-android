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
public class ParcelableClassModule extends AbstractView implements IClassModule {

	
	@Override
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		
		// writeToParcel()
		renderWriteToParcel(sb, ns, klass, idt);
		
		// class constructor via parcel
		renderParcelConstructor(sb, ns, klass, idt);
		
		// static final Parcelable.Creator<?> CREATOR
		renderParcelableCreator(sb, ns, klass, idt);
		
		// describeContents()
		renderDescribeContents(sb, idt);
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("android.os.Parcel");
		imports.add("android.os.Parcelable");
		return imports;
	}
	
	/**
	 * Generates the parcel serializator.
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param klass Class to render
	 * @param idt Indent
	 */
	private void renderWriteToParcel(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		final String indent = getIndent(idt);
		
		// method header
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Flatten this object into a Parcel.\n");
		sb.append(indent).append(" * @param parcel the Parcel in which the object should be written.\n");
		sb.append(indent).append(" * @param flags additional flags about how the object should be written.\n");
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public void writeToParcel(Parcel parcel, int flags) {\n");

		// members
		if (klass.doesExtend()) {
			sb.append(indent).append("	super.writeToParcel(parcel, flags);\n");
		}
		for (JavaMember member : klass.getMembers()) {
			renderWriteToParcel(sb, ns, member, idt + 1);
		}
		sb.append(indent).append("}\n");
	}
	
	/**
	 * Generates the parcel serializator for one member.
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param member Member to render
	 * @param idt Indent
	 */
	private void renderWriteToParcel(StringBuilder sb, Namespace ns, JavaMember member, int idt) {
		final String indent = getIndent(idt);
		
		if (member.isEnum()) {
			// TODO
			sb.append(indent).append("/* TODO enum: ").append(member.getName()).append(" */\n");
		} else {
			
			final JavaClass klass = member.getType();
			if (klass.isArray()) {
				final JavaClass arrayType = klass.getArrayType();
				
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
	
	/**
	 * Generates the constructor via parcel
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param klass Class to render
	 * @param idt Indent
	 */
	private void renderParcelConstructor(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		final String indent = getIndent(idt);
		
		// header
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Construct via parcel.\n");
		sb.append(indent).append(" */\n");
		
		// signature
		sb.append(indent).append("protected ");
		sb.append(getClassName(klass));
		sb.append("(Parcel parcel) {\n");

		// members
		if (klass.doesExtend()) {
			sb.append(indent).append("	super(parcel);\n");
		}
		for (JavaMember member : klass.getMembers()) {
			renderParcelConstructor(sb, ns, member, idt + 1);
		}
		sb.append(indent).append("}\n");
	}
	
	/**
	 * Generates the parcel de-serializator for one member.
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param member Member to render
	 * @param idt Indent
	 */
	private void renderParcelConstructor(StringBuilder sb, Namespace ns, JavaMember member, int idt) {
		final String indent = getIndent(idt);
		
		if (member.isEnum()) {
			// TODO
			sb.append(indent).append(member.getName());
			sb.append(" = null; // TODO enum\n");
		} else {
			
			final JavaClass klass = member.getType();
			if (klass.isArray()) {
				final JavaClass arrayType = klass.getArrayType();
				
				// like: final int genreSize = parcel.readInt();
				sb.append(indent).append("final int ");
				sb.append(member.getName());
				sb.append("Size = parcel.readInt();\n");
				
				// like: genre = new ArrayList<String>(genreSize);
				sb.append(indent).append(member.getName());
				sb.append(" = new ArrayList<");
				sb.append(getClassReference(ns, arrayType));
				sb.append(">(");
				sb.append(member.getName());
				sb.append("Size);\n");
				
				// like: for (int i = 0; i < genreSize; i++) {
				sb.append(indent).append("for (int i = 0; i < ");
				sb.append(member.getName());
				sb.append("Size; i++) {\n");
				
				// like: genre.add(parcel.readString());
				sb.append(indent).append("\t");
				sb.append(member.getName());
				sb.append(".add(parcel.");
				sb.append(getUnparcelStatement(ns, arrayType));
				sb.append(");\n");
				
				sb.append(indent).append("}\n");
				
			} else {
				// like: artist = parcel.readString();
				sb.append(indent).append(member.getName());
				sb.append(" = parcel.");
				sb.append(getUnparcelStatement(ns, klass));
				sb.append(";\n");
			}
		}
	}
	
	/**
	 * Returns the right unparcel statement for a given type.
	 * @param ns Namespace reference
	 * @param k Given type
	 * @return
	 */
	private String getUnparcelStatement(Namespace ns, JavaClass k) {
		if (k.isNative()) {
			final String typeName = k.getName();
			if (typeName.equals("integer")) {
				return "readInt()";
				
			} else if (typeName.equals("string")) {
				return "readString()";
				
			} else if (typeName.equals("boolean")) {
				return "readInt() == 1";

			} else if (typeName.equals("number")) {
				return "readDouble()";
			}
				
		} else {
			final String classRef = getClassReference(ns, k);
			return "<" + classRef + ">readParcelable(" + classRef + ".class.getClassLoader())";
		}
		
		throw new IllegalArgumentException("Don't know how to unparcel Class " + k.getName() + ".");
	}
	
	/**
	 * Generates the static CREATOR class that creates an object from parcel
	 * (basically just calls the constructor that does the work.)
	 * 
	 * @param sb Current StringBuilder
	 * @param ns Current namespace
	 * @param klass Class to render
	 * @param idt Indent
	 */
	private void renderParcelableCreator(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {
		final String indent = getIndent(idt);
		final String n = getClassName(klass);
		
		// variable comment block
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Generates instances of this Parcelable class from a Parcel.\n");
		sb.append(indent).append(" */\n");
		
		// signature & body
		sb.append(indent).append("public static final Parcelable.Creator<").append(n).append("> CREATOR = new Parcelable.Creator<").append(n).append(">() {\n");
		sb.append(indent).append("	@Override\n");
		sb.append(indent).append("	public ").append(n).append(" createFromParcel(Parcel parcel) {\n");
		sb.append(indent).append("		return new ").append(n).append("(parcel);\n");
		sb.append(indent).append("	}\n");
		sb.append(indent).append("	@Override\n");
		sb.append(indent).append("	public ").append(n).append("[] newArray(int n) {\n");
		sb.append(indent).append("		return new ").append(n).append("[n];\n");
		sb.append(indent).append("	}\n");
		sb.append(indent).append("};\n");
	}

	private void renderDescribeContents(StringBuilder sb, int idt) {
		final String indent = getIndent(idt);
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public int describeContents() {\n");
		sb.append(indent).append("	return 0;\n");
		sb.append(indent).append("}\n");
	}
	
}
