package org.xbmc.android.jsonrpc.generator.controller;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.android.jsonrpc.generator.model.Constructor;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;
import org.xbmc.android.jsonrpc.generator.model.Parameter;

public class ConstructorController {

	private final Klass type;
	
	public ConstructorController(Klass type) {
		this.type = type;
	}
	
	public List<Constructor> getConstructors() {
		final List<Constructor> constructors = new ArrayList<Constructor>();
		
		// for non-multitype, just create one constructor with all properties
		if (!type.isMultiType()) {
			final Constructor c = new Constructor(type);
			for (Member m : type.getMembers()) {
				if (m.isEnum()) {
					c.addParameter(new Parameter(m.getName(), m.getEnum()));
				} else {
					c.addParameter(new Parameter(m.getName(), m.getType()));
				}
			}
			constructors.add(c);
			
		// for multitypes, we need a constructor per type (member)	
		} else {
			
			for (Member m : type.getMembers()) {
				final Constructor c = new Constructor(type);
				if (m.isEnum()) {
					c.addParameter(new Parameter(m.getName(), m.getEnum()));
				} else {
					c.addParameter(new Parameter(m.getName(), m.getType()));
				}
				constructors.add(c);
			}
		}
		
		return constructors;
	}
}
