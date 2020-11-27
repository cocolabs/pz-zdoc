package io.yooksi.pz.luadoc.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a parsed lua class reference.
 */
public class LuaClass extends MemberClass {

	private final @Nullable String type;

	public LuaClass(String name, @NotNull String type) {
		super(name);
		this.type = type;
	}

	public LuaClass(String name) {
		super(name);
		this.type = null;
	}

	public @Nullable String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type);
	}
}
