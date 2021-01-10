/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.list.SetUniqueList;

public enum ModifierKey {

	STATIC("static", Modifier.STATIC),
	ABSTRACT("abstract", Modifier.ABSTRACT),
	FINAL("final", Modifier.FINAL),
	SYNCHRONIZED("synchronized", Modifier.SYNCHRONIZED),
	UNDECLARED("", 0x00000000);

	private static final ModifierKey[] VALUES = Arrays.stream(ModifierKey.values())
			.filter(v -> v != UNDECLARED).collect(Collectors.toList()).toArray(new ModifierKey[]{});

	public final String name;
	final int value;

	ModifierKey(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public static SetUniqueList<ModifierKey> get(String...elements) {

		List<ModifierKey> result = new ArrayList<>();
		for (String element : elements)
		{
			Arrays.stream(VALUES).filter(v -> v.name.equals(element))
					.findFirst().ifPresent(result::add);
		}
		if (result.isEmpty()) {
			result.add(UNDECLARED);
		}
		return SetUniqueList.setUniqueList(result);
	}

	public static SetUniqueList<ModifierKey> get(int modifiers) {

		List<ModifierKey> result = Arrays.stream(ModifierKey.values())
				.filter(v -> (modifiers & v.value) != 0)
				.collect(Collectors.toList());

		if (result.isEmpty()) {
			result.add(UNDECLARED);
		}
		return SetUniqueList.setUniqueList(result);
	}

	@Override
	public String toString() {
		return name;
	}
}
