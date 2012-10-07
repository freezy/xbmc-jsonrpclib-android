package org.xbmc.android.jsonrpc.generator.introspect;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;

@JsonIgnoreProperties({ "default" })
public class Type extends Property {

	@JsonProperty("extends")
	private ExtendsWrapper extendsValue;

	private String id;

	public ExtendsWrapper getExtends() {
		return extendsValue;
	}

	public void setExtends(ExtendsWrapper extendsValue) {
		this.extendsValue = extendsValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		return type.toString();
	}

}
