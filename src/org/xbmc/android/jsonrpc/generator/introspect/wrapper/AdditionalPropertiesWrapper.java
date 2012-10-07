package org.xbmc.android.jsonrpc.generator.introspect.wrapper;

public class AdditionalPropertiesWrapper {
	
	private final Boolean available;
	private final TypeWrapper type;
	
	public AdditionalPropertiesWrapper(Boolean available) {
		this.available = available;
		this.type = null;
	}
	
	public AdditionalPropertiesWrapper(TypeWrapper type) {
		this.available = null;
		this.type = type;
	}

	public Boolean getAvailable() {
		return available != null && available;
	}

	public TypeWrapper getType() {
		return type;
	}
}
