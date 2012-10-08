package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

public class Klass {

	private final String name;
	private final String apiType;

	private boolean isNative = false;
	private boolean isInner = false;

	private final List<Member> members = new ArrayList<Member>();
	private final List<Klass> innerTypes = new ArrayList<Klass>();

	public Klass(String name, String apiType) {
		this.name = name;
		this.apiType = apiType;
	}

	public void addMember(Member member) {
		members.add(member);
	}
	
	public void addInnerType(Klass klass) {
		innerTypes.add(klass);
	}
	
	public boolean hasInnerTypes() {
		return !innerTypes.isEmpty();
	}

	public boolean isNative() {
		return isNative;
	}
	
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

	public String getName() {
		return name;
	}

	public String getApiType() {
		return apiType;
	}

	public List<Member> getMembers() {
		return members;
	}
	
	public List<Klass> getInnerTypes() {
		return innerTypes;
	}

}
