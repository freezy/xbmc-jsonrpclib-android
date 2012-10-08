package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Enum;


public class EnumView extends AbstractView {
	
	private final static String DISPLAY_ONLY = "Application.Property.Value";
	
	private final Enum e;
	
	public EnumView(Enum e) {
		this.e = e;
	}
	
	public String render(int indent, boolean force) {
		
		// debug
		if (!force && !DISPLAY_ONLY.isEmpty() && !e.getApiType().equals(DISPLAY_ONLY)) {
			return "";
		}
		
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		
		final StringBuilder sb = new StringBuilder("\n");
		sb.append(prefix).append("public static enum ");
		if (e.isInner()) {
			sb.append(getInnerType(e.getName()));
		} else {
			sb.append(e.getName());
		}
		sb.append(" {\n");
		
		for (String enumValue : e.getValues()) {
			sb.append(prefix).append("\t");
			sb.append(enumValue.toUpperCase());
			sb.append("(\"");
			sb.append(enumValue);
			sb.append("\");\n");
		}
		
		sb.append(prefix).append("}\n");
		
		return sb.toString();
	}
	
	public static String getInnerType(String type) {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
}
