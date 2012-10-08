package org.xbmc.android.jsonrpc.generator.model;

public class Parameter {

	private final String name;
	private final Klass type;
	private final Enum e;

	private String description;

	public Parameter(String name, Klass type) {
		this.name = name;
		this.type = type;
		this.e = null;
	}

	public Parameter(String name, Enum e) {
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public boolean isEnum() {
		return e != null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public Klass getType() {
		return type;
	}

	public Enum getEnum() {
		return e;
	}
	
}
