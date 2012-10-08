package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Member;


public class MemberView extends AbstractView {
	
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
		sb.append(getClassName(member));
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
	

	
}
