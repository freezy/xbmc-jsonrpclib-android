package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

public class ConstructorView {

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
		sb.append(prefix).append("public ");
		sb.append(constructor.getClassName());
		sb.append("(");
		for(Parameter p : constructor.getParameters()) {
			if (p.isEnum()) {
				sb.append(p.getEnum().getName());
			} else {
				sb.append(p.getType().getName());
			}
			sb.append(" ");
			sb.append(p.getName());
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(") {\n");
		
		sb.append(prefix).append("}\n");

		return sb.toString();
	}
}
