package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Namespace {

	private static HashMap<String, Namespace> namespaces = new HashMap<String, Namespace>();

	private final String name;
	private final List<Klass> classes = new ArrayList<Klass>();
	private final List<Enum> enums = new ArrayList<Enum>();

	public Namespace(String name) {
		this.name = name;
	}

	public void addClass(Klass klass) {
		classes.add(klass);
	}

	public void addEnum(Enum e) {
		enums.add(e);
	}

	public String getName() {
		return name;
	}

	public List<Klass> getClasses() {
		return classes;
	}

	public List<Enum> getEnums() {
		return enums;
	}

	public static Namespace get(String name) {
		// trim suffixes if provided
		if (name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		if (!namespaces.containsKey(name)) {
			namespaces.put(name, new Namespace(name));
		}
		return namespaces.get(name);
	}

	public static Collection<Namespace> getAll() {
		return namespaces.values();
	}

}
