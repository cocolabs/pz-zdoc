package io.yooksi.pz.luadoc.element;

import io.yooksi.pz.luadoc.lang.ParseResult;

public abstract class MemberClass implements ParseResult {

	protected final String name;

	public MemberClass(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract String toString();
}
