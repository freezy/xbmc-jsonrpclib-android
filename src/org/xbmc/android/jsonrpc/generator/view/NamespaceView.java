package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

public class NamespaceView extends AbstractView {
	
	private final static String DISPLAY_ONLY = "Application";
	
	private final Namespace namespace;
	private final String packageName;
	
	public NamespaceView(Namespace namespace, String packageName) {
		this.namespace = namespace;
		this.packageName = packageName;
	}
	
	public String render() {
		
		// debug
		if (!DISPLAY_ONLY.isEmpty() && !namespace.getName().equals(DISPLAY_ONLY)) {
			return "";
		}
		
		// init
		final StringBuilder sb = new StringBuilder();
		
		// package
		sb.append("package ").append(packageName).append(";\n");
		
		// signature
		sb.append("\n");
		sb.append("public final class ");
		sb.append(namespace.getName());
		sb.append(" {\n");
		
		// classes
		for (Klass klass : namespace.getClasses()) {
			final ClassView classView = new ClassView(klass);
			sb.append(classView.renderDeclaration(1, false));
		}
		
		// enum
		for (Enum e : namespace.getEnums()) {
			final EnumView enumView = new EnumView(e);
			sb.append(enumView.render(1, false));
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}

}
