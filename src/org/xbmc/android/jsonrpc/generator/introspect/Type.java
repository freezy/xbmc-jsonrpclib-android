package org.xbmc.android.jsonrpc.generator.introspect;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.ExtendsWrapper;

/**
 * A global type as defined in introspect's "types" collection.
 * 
 * Only two additional attributes compared to {@link Property}:
 * {@link Type#getId()} and {@link Type#getExtends()}.
 * 
 * @author freezy <freezy@xbmc.org>
 */
@JsonIgnoreProperties({ "default" })
public class Type extends Property {

	@JsonProperty("extends")
	private ExtendsWrapper extendsValue;

	private String id;

	/**
	 * Returns true if this type extends another type, false otherwise.
	 * @return True if extends, false otherwise.
	 */
	public boolean doesExtend() {
		return extendsValue != null;
	}
	
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
