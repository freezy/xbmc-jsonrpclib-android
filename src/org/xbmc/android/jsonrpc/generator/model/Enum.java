package org.xbmc.android.jsonrpc.generator.model;

public class Enum {

	private final String name;
	private final String apiType;

	public Enum(String name, String apiType) {
		this.name = name;
		this.apiType = apiType;
	}

	public String getName() {
		return name;
	}

	public String getApiType() {
		return apiType;
	}

}
