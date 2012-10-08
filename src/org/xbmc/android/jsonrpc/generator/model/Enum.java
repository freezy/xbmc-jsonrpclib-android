package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

public class Enum {

	private final String name;
	private final String apiType;
	private final List<String> values = new ArrayList<String>();
	
	private boolean isInner = false;

	public Enum(String name, String apiType) {
		this.name = name;
		this.apiType = apiType;
	}
	
	public void addValue(String value) {
		values.add(value);
	}

	public String getName() {
		return name;
	}

	public String getApiType() {
		return apiType;
	}

	public List<String> getValues() {
		return values;
	}

	public boolean isInner() {
		return isInner;
	}

	public void setInner(boolean isInner) {
		this.isInner = isInner;
	}

}
