package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Member;


public class MemberView {
	
	private final Member member;
	
	public MemberView(Member member) {
		this.member = member;
	}
	
	public String renderDeclaration(int indent) {
		
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("public final ");
		
		if (!member.isEnum()) {
			if (member.getType().isNative()) {
				sb.append(getNativeType(member.getType().getName()));
			} else if (member.getType().isInner()) {
				sb.append(ClassView.getInnerType(member.getType().getName()));
			} else {
				sb.append(member.getType().getName());
			}
		} else {
			sb.append(EnumView.getInnerType(member.getEnum().getName()));
		}
		sb.append(" ").append(member.getName());
		sb.append(";\n");
		
		return sb.toString();
	}
	
	public String renderFieldDeclaration(int indent) {
		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(prefix).append("public static final String ");
		sb.append(member.getName().toUpperCase());
		sb.append(" = \"");
		sb.append(member.getName());
		sb.append("\";\n");
		return sb.toString();
	}
	
	private String getNativeType(String type) {
		if (type.equals("boolean")) {
			return "Boolean";
		} else if (type.equals("integer")) {
			return "Integer";
		} else if (type.equals("string")) {
			return "String";
		} else {
			throw new IllegalArgumentException("Unknown native type \"" + type + "\".");
		}
	}
	
}
