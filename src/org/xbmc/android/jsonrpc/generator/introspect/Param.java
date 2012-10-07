package org.xbmc.android.jsonrpc.generator.introspect;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({ "default" })
public class Param extends Property {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		if (type != null) {
			return type.toString();
		}
		return "unknown type";
	}
}
