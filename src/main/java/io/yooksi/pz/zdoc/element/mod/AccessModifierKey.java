/*
 * Copyright (C) 2020 Matthew Cain
 * ZomboidDoc - Lua library compiler for Project Zomboid
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc.element.mod;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

public enum AccessModifierKey {

	PUBLIC("public", Modifier.PUBLIC),
	PROTECTED("protected", Modifier.PROTECTED),
	PRIVATE("private", Modifier.PRIVATE),
	DEFAULT("", 0x00000000);

	private static final AccessModifierKey[] VALUES = Arrays.stream(AccessModifierKey.values())
			.filter(v -> v != DEFAULT).collect(Collectors.toList()).toArray(new AccessModifierKey[]{});

	public final String name;
	final int value;

	AccessModifierKey(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public static AccessModifierKey get(@Nullable String name) {

		if (name == null) {
			return DEFAULT;
		}
		// trim the name before comparison
		final String findName = name.trim();

		return Arrays.stream(VALUES).filter(v -> v.name.equals(findName))
				.findFirst().orElse(DEFAULT);
	}

	public static AccessModifierKey get(int modifiers) {
		return Arrays.stream(VALUES).filter(v -> (modifiers & v.value) != 0)
				.findFirst().orElse(DEFAULT);
	}

	@Override
	public String toString() {
		return name;
	}
}
