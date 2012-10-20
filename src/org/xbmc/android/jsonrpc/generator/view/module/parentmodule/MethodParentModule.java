package org.xbmc.android.jsonrpc.generator.view.module.parentmodule;

import java.util.HashSet;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IParentModule;

public class MethodParentModule extends AbstractView implements IParentModule {

	@Override
	public void renderExtends(StringBuilder sb, JavaClass klass) {
		
		final JavaMethod m = getMethod(klass);
		final JavaClass returnType = m.getReturnType();
		
		sb.append(" extends AbstractCall<");
		if (returnType.isArray()) {
			sb.append(getClassReference(klass.getNamespace(), returnType.getArrayType()));
		} else {
			sb.append(getClassReference(klass.getNamespace(), returnType));
		}
		sb.append(">");
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("org.xbmc.android.jsonrpc.api.AbstractCall");
		return imports;
	}
	
	private JavaMethod getMethod(JavaClass klass) {
		if (!(klass instanceof JavaMethod)) {
			throw new IllegalArgumentException("Superclass for method can only be found with a JavaMethod object.");
		}
		final JavaMethod m = (JavaMethod)klass;
		if (m.getReturnType() == null) {
			throw new IllegalStateException("Return type of " + klass.getName() + " cannot be null.");
		}

		return m;
	}

}
