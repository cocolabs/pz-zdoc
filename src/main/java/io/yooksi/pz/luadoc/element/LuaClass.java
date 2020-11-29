package io.yooksi.pz.luadoc.element;

import java.util.Collection;

import io.yooksi.pz.luadoc.lang.EmmyLua;

/**
 * This class represents a parsed lua class reference.
 */
public class LuaClass extends MemberClass {

	private final String type;

	public LuaClass(String name, String type) {
		super(name);
		this.type = type;
	}

	public LuaClass(String name) {
		super(name);
		this.type = "";
	}

	public Collection<String> writeTo(Collection<String> content, boolean annotate) {

		if (annotate)
		{
			content.add(EmmyLua.CLASS.create(type.isEmpty() ?
					new String[]{ name } : new String[]{ name, type }));
		}
		content.add(name + " = {}");
		content.add("");
		return content;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type);
	}
}
