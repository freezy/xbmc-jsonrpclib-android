package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

public class Constructor {

	private final String className;
	private final List<Parameter> parameters = new ArrayList<Parameter>();
	
	public Constructor(String className) {
		this.className = className;
	}
	
	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getClassName() {
		return className;
	}
}
