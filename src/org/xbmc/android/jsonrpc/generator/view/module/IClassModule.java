package org.xbmc.android.jsonrpc.generator.view.module;

import java.util.Set;

import org.xbmc.android.jsonrpc.generator.model.Klass;

public interface IClassModule {

	public void render(StringBuilder sb, int indent, Klass klass);
	public Set<String> getImports(Klass klass);
}
