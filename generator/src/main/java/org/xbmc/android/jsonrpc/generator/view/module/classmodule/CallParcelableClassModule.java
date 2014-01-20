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
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IClassModule;

/**
 * Provides Parcelable-serialization via Android.
 *
 * @author freezy <freezy@xbmc.org>
 */
public class CallParcelableClassModule extends AbstractView implements IClassModule {

	@Override
	public void render(StringBuilder sb, Namespace ns, JavaClass klass, int idt) {

		if (!(klass instanceof JavaMethod)) {
			throw new IllegalArgumentException("When rendering method API class modules, passed class must be of type JavaMethod.");
		}
		final JavaMethod method = (JavaMethod)klass;

		// writeToParcel()
		renderWriteToParcel(sb, ns, method, idt);

		// class constructor via parcel
		renderParcelConstructor(sb, ns, klass, idt);

		// static final Parcelable.Creator<?> CREATOR
		renderParcelableCreator(sb, ns, method, idt);
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
	private void renderWriteToParcel(StringBuilder sb, Namespace ns, JavaMethod klass, int idt) {
		final String indent = getIndent(idt);

		// method header
/*		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Flatten this object into a Parcel.\n");
		sb.append(indent).append(" * @param parcel the Parcel in which the object should be written.\n");
		sb.append(indent).append(" * @param flags additional flags about how the object should be written.\n");
		sb.append(indent).append(" * /\n");
*/

		// signature
		sb.append("\n");
		sb.append(indent).append("@Override\n");
		sb.append(indent).append("public void writeToParcel(Parcel parcel, int flags) {\n");

		// body
		sb.append(indent).append("	super.writeToParcel(parcel, flags);\n");
		if (klass.getReturnType().isNative()) {
			sb.append(indent).append("	parcel.writeValue(mResult);\n");
		} else {
			sb.append(indent).append("	parcel.writeParcelable(mResult, flags);\n");
		}

		sb.append(indent).append("}\n");
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
		sb.append("\n");
		sb.append(indent).append("/**\n");
		sb.append(indent).append(" * Construct via parcel.\n");
		sb.append(indent).append(" */\n");

		// signature
		sb.append(indent).append("protected ");
		sb.append(getClassName(klass));
		sb.append("(Parcel parcel) {\n");

		// body
		sb.append(indent).append("	super(parcel);\n");

		sb.append(indent).append("}\n");
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
	private void renderParcelableCreator(StringBuilder sb, Namespace ns, JavaMethod klass, int idt) {
		final String indent = getIndent(idt);
		final String n = getClassName(klass);

		// variable comment block
		sb.append("\n");
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

}
