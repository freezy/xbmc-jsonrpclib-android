package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Enum;


public class EnumView {
	
	private final static String DISPLAY_ONLY = "Application.Property.Value";
	
	private final Enum e;
	
	public EnumView(Enum e) {
		this.e = e;
	}
	
	public String render(int indent) {
		
		// debug
		if (!DISPLAY_ONLY.isEmpty() && !e.getApiType().equals(DISPLAY_ONLY)) {
			return "";
		}
		
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("public static enum ");
		sb.append(e.getName());
		sb.append(" {\n");
		sb.append(prefix).append("}\n");
		
		return sb.toString();
	}
}
