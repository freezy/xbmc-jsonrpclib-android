package org.xbmc.android.jsonrpc.generator.introspect.wrapper;

import java.util.List;

public class ExtendsWrapper {
	private final String name;
	private final List<String> array;

	public ExtendsWrapper(String name) {
		this.name = name;
		array = null;
	}

	public ExtendsWrapper(List<String> array) {
		this.name = null;
		this.array = array;
	}

	public String getName() {
		return name;
	}
	
	public List<String> getArray() {
		return array;
	}

}