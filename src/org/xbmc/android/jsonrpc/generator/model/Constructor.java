package org.xbmc.android.jsonrpc.generator.model;

import java.util.ArrayList;
import java.util.List;

public class Constructor {

	private final Klass type;
	private final List<Parameter> parameters = new ArrayList<Parameter>();

	public Constructor(Klass type) {
		this.type = type;
	}

	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public Klass getType() {
		return type;
	}

}
