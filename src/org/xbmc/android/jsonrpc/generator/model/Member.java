package org.xbmc.android.jsonrpc.generator.model;

public class Member {

	private final String name;
	private final Klass type;
	private final Enum e;

	public Member(String name, Klass type) {
		this.name = name;
		this.type = type;
		this.e = null;
	}
	
	public Member(String name, Enum e) {
		this.name = name;
		this.type = null;
		this.e = e;
	}
	
	public boolean isEnum() {
		return e != null;
	}
	
	public boolean isInner() {
		return type != null && type.isInner();
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
