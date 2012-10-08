package org.xbmc.android.jsonrpc.generator.view;

import org.xbmc.android.jsonrpc.generator.model.Enum;
import org.xbmc.android.jsonrpc.generator.model.Klass;
import org.xbmc.android.jsonrpc.generator.model.Member;

public class ClassView {

	private final static String DISPLAY_ONLY = "Application.Property.Value";

	private final Klass klass;

	public ClassView(Klass klass) {
		this.klass = klass;
	}

	public String renderDeclaration(int indent, boolean force) {

		// debug
		if (!force && !DISPLAY_ONLY.isEmpty() && !klass.getApiType().equals(DISPLAY_ONLY)) {
			return "";
		}

		String prefix = "";
		for (int i = 0; i < indent; i++) {
			prefix += "\t";
		}

		final StringBuilder sb = new StringBuilder("\n");
		sb.append(prefix).append("public static class ");
		if (klass.isInner()) {
			sb.append(getInnerType(klass.getName()));
		} else {
			sb.append(klass.getName());
		}
		sb.append(" {\n");

		// field names
		sb.append("\n").append(prefix).append("\t// field names\n");
		for (Member member : klass.getMembers()) {
			final MemberView memberView = new MemberView(member);
			sb.append(memberView.renderFieldDeclaration(indent + 1));
		}

		// members
		sb.append("\n").append(prefix).append("\t// class members\n");
		for (Member member : klass.getMembers()) {
			final MemberView memberView = new MemberView(member);
			sb.append(memberView.renderDeclaration(indent + 1));
		}

		// inner classes
		if (klass.hasInnerTypes()) {
			for (Klass innerClass : klass.getInnerTypes()) {
				final ClassView classView = new ClassView(innerClass);
				sb.append(classView.renderDeclaration(indent + 1, true));
			}
		}

		// inner enums
		if (klass.hasInnerEnums()) {
			for (Enum e : klass.getInnerEnums()) {
				final EnumView enumView = new EnumView(e);
				sb.append(enumView.render(indent + 1, true));
			}
		}

		sb.append(prefix).append("}\n");

		return sb.toString();
	}

	public static String getInnerType(String type) {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
}
