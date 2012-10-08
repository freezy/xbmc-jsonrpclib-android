package org.xbmc.android.jsonrpc.generator.introspect;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * A parameter, as used in the <tt>params</tt> field of a method.
 * 
 * It extends {@link property} and additionally contains the {@link #name}
 * of the parameter.
 * 
 * @author freezy <freezy@xbmc.org>
 */
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
