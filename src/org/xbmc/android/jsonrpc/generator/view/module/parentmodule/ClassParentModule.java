package org.xbmc.android.jsonrpc.generator.view.module.parentmodule;

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

}
