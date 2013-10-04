package org.xbmc.android.jsonrpc.generator.view.module.parentmodule;

import java.util.HashSet;
import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.JavaClass;
import org.xbmc.android.jsonrpc.generator.model.JavaMethod;
import org.xbmc.android.jsonrpc.generator.model.Namespace;
import org.xbmc.android.jsonrpc.generator.view.AbstractView;
import org.xbmc.android.jsonrpc.generator.view.module.IParentModule;

public class MethodParentModule extends AbstractView implements IParentModule {

	@Override
	public void renderExtends(StringBuilder sb, JavaClass klass) {

		final JavaMethod m = getMethod(klass);
		final JavaClass returnArrayType = m.getReturnType().isTypeArray() ? m.getReturnType().getArrayType() : m.getReturnType();

		sb.append(" extends AbstractCall<");

		if (returnArrayType.isInner()) {
			sb.append(getInnerClassReference(klass, returnArrayType));
		} else {
			sb.append(getClassReference(klass.getNamespace(), returnArrayType));
		}
		sb.append(">");
	}

	private Object getInnerClassReference(JavaClass parent, JavaClass klass) {
		final StringBuilder sb = new StringBuilder();

		final String className = getClassName(parent.getNamespace(), klass);
		if (!klass.isNative() && !className.startsWith("List")) {
			sb.append(parent.getName());
			sb.append(".");
		}
		sb.append(className);
		return sb.toString();
	}

	@Override
	public Set<String> getImports(JavaClass klass) {
		final Set<String> imports = new HashSet<String>();
		imports.add("org.xbmc.android.jsonrpc.api.AbstractCall");

		final JavaMethod m = getMethod(klass);
		final JavaClass returnType = m.getReturnType();
		final JavaClass returnArrayType = m.getReturnType().isTypeArray() ? m.getReturnType().getArrayType() : m.getReturnType();

		final Namespace ns = returnType.isTypeArray() ? returnType.getArrayType().getNamespace() : returnType.getNamespace();

		if (!ns.equals(klass.getNamespace())) {
			imports.add(ns.getPackageName() + "." + ns.getName());
		}

		// dirty hack
		final Set<String> arrayTypes = returnArrayType.getImports();
/*		for (String t : arrayTypes) {
			if (!t.endsWith("AbstractModel")) {
				System.out.println(t);
			}
		}*/
		imports.addAll(arrayTypes);
		return imports;
	}

	/**
	 * Casts a class to method and asserts stuff.
	 * @param klass
	 * @return
	 */
	private JavaMethod getMethod(JavaClass klass) {
		if (!(klass instanceof JavaMethod)) {
			throw new IllegalArgumentException("Cannot apply parent module for methods to normal classes.");
		}
		final JavaMethod m = (JavaMethod)klass;
		if (m.getReturnType() == null) {
			throw new IllegalStateException("Return type of " + klass.getName() + " cannot be null.");
		}

		return m;
	}

}
