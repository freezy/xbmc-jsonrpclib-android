package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

public class NamespaceView {
	
	private final Namespace namespace;
	
	public NamespaceView(Namespace namespace) {
		this.namespace = namespace;
	}
	
	public String render() {
		final StringBuilder sb = new StringBuilder();
		sb.append("public final class ");
		sb.append(namespace.getName());
		sb.append(" {\n");
		
		for (Klass klass : namespace.getClasses()) {
			final ClassView classView = new ClassView(klass);
			sb.append(classView.render(1));
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}

}
