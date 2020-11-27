package io.yooksi.pz.luadoc.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.lang.ParseResult;

public class MemberClass implements ParseResult {

	private final String name;
	private final @Nullable String type;

	public MemberClass(String name, @NotNull String type) {
		this.name = name;
		this.type = type;
	}

	public MemberClass(String name) {
		this.name = name;
		this.type = null;
	}

	public String getName() {
		return name;
	}

	public @Nullable String getType() {
		return type;
	}
}
