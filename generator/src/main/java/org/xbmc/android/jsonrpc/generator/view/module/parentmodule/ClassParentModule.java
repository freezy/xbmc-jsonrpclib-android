package org.xbmc.android.jsonrpc.generator.view.module.parentmodule;

import java.util.HashSet;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IParentModule;

public class ClassParentModule extends AbstractView implements IParentModule {

	@Override
	public void renderExtends(StringBuilder sb, JavaClass klass) {
		
		sb.append(" extends ");
		if (klass.doesExtend()) {
			sb.append(getClassReference(klass.getNamespace(), klass.getParentClass()));
		} else {
			sb.append("AbstractModel");
		}
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		if (!klass.doesExtend()) {
			imports.add("org.xbmc.android.jsonrpc.api.AbstractModel");
		}
		return imports;
	}

}
