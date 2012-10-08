package org.xbmc.android.jsonrpc.generator.model;

public class Member {

	private final String name;
	private final Klass type;

	public Member(String name, Klass type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Klass getType() {
		return type;
	}

}
