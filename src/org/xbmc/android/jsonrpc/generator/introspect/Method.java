package org.xbmc.android.jsonrpc.generator.introspect;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.xbmc.android.jsonrpc.generator.introspect.wrapper.TypeWrapper;

@JsonIgnoreProperties( { "type" }) // type is always "method"
public class Method {

	private String description;
	private List<Param> params;
	private TypeWrapper returns;

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Param> getParams() {
		return params;
	}
	public void setParams(List<Param> params) {
		this.params = params;
	}
	public TypeWrapper getReturns() {
		return returns;
	}
	public void setReturns(TypeWrapper returns) {
		this.returns = returns;
	}
}
