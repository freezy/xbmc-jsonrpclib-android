package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Klass;


public class ClassView {
	
	private final static String DISPLAY_ONLY = "Application.Property.Value";
	
	private final Klass klass;
	
	public ClassView(Klass klass) {
		this.klass = klass;
	}
	
	public String render(int indent) {
		
		// debug
		if (!DISPLAY_ONLY.isEmpty() && !klass.getApiType().equals(DISPLAY_ONLY)) {
			return "";
		}
		
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
