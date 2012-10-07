package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Klass;


public class ClassView {
	
	private final Klass klass;
	
	public ClassView(Klass klass) {
		this.klass = klass;
	}
	
	public String render(int indent) {
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("public static class ");
		sb.append(klass.getName());
		sb.append(" {\n");
		sb.append(prefix).append("}\n");
		
		return sb.toString();
	}
}
