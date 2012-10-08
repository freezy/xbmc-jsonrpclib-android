package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

public class Klass {

	private final String name;
	private final String apiType;

	private boolean isNative = false;
	private boolean isInner = false;
	private boolean isMultiType = false;

	private final List<Constructor> constructors = new ArrayList<Constructor>();
	private final List<Member> members = new ArrayList<Member>();
	private final List<Klass> innerTypes = new ArrayList<Klass>();
	private final List<Enum> innerEnums = new ArrayList<Enum>();

	public Klass(String name) {
		this(name, null);
	}

	public Klass(String name, String apiType) {
		this.name = name;
		this.apiType = apiType;
	}

	public void addConstructor(Constructor c) {
		constructors.add(c);
	}

	public void addMember(Member member) {
		members.add(member);
	}

	public void addInnerType(Klass klass) {
		innerTypes.add(klass);
	}

	public void addInnerEnum(Enum e) {
		innerEnums.add(e);
	}

	public boolean hasInnerTypes() {
		return !innerTypes.isEmpty();
	}

	public boolean hasInnerEnums() {
		return !innerEnums.isEmpty();
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

	public boolean isMultiType() {
		return isMultiType;
	}

	public void setMultiType(boolean isMultiType) {
		this.isMultiType = isMultiType;
	}

	public String getName() {
		return name;
	}

	public String getApiType() {
		return apiType;
	}

	public List<Constructor> getConstructors() {
		return constructors;
	}

	public List<Member> getMembers() {
		return members;
	}

	public List<Klass> getInnerTypes() {
		return innerTypes;
	}

	public List<Enum> getInnerEnums() {
		return innerEnums;
	}

}
