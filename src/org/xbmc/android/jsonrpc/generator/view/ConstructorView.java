package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

public class ConstructorView extends AbstractView {

	private final Constructor constructor;

	public ConstructorView(Constructor constructor) {
		this.constructor = constructor;
	}

	public String renderDeclaration(int indent) {

		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}

		final StringBuilder sb = new StringBuilder("\n");
		
		// signature
		sb.append(prefix).append("public ");
		sb.append(getClassName(constructor.getType()));
		sb.append("(");
		for (Parameter p : constructor.getParameters()) {
			sb.append(getClassName(p));
			sb.append(" ");
			sb.append(p.getName());
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(") {\n");
		
		// body
		for (Parameter p : constructor.getParameters()) {
			sb.append(prefix).append("\tthis.");
			sb.append(p.getName());
			sb.append(" = ");
			sb.append(p.getName());
			sb.append(";\n");
		}
		
		
		sb.append(prefix).append("}\n");

		return sb.toString();
	}
}
