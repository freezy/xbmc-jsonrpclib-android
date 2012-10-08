package org.xbmc.android.jsonrpc.generator.controller;

import org.xbmc.android.jsonrpc.generator.introspect.Type;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Namespace;

public class TypeController {

	private final Type type;
	private final String name;
	
	public TypeController(String name, Type type) {
		this.type = type;
		this.name = name;
	}
	
	public void register() {
		final Namespace ns = Namespace.get(name);
		final String strippedName = name.contains(".") ? name.substring(name.indexOf(".") + 1) : name;
		final Klass klass = new Klass(strippedName, name);
		
		ns.addClass(klass);
	}
}
