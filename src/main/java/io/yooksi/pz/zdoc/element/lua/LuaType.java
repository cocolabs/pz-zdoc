/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.yooksi.pz.zdoc.element.lua;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import com.google.common.collect.ImmutableList;

import io.yooksi.pz.zdoc.element.IClass;

public class LuaType implements IClass {

	final String name;
	private final @UnmodifiableView List<LuaType> otherTypes;

	public LuaType(String name, List<LuaType> otherTypes) {
		this.name = name;
		this.otherTypes = Collections.unmodifiableList(otherTypes);
	}

	public LuaType(String name, LuaType otherType) {
		this.name = name;
		this.otherTypes = ImmutableList.of(otherType);
	}

	public LuaType(String name) {
		this(name, new ArrayList<>());
	}

	public String getName() {
		return name;
	}

	@Override
	public @UnmodifiableView List<LuaType> getTypeParameters() {
		return otherTypes;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LuaType luaType = (LuaType) o;

		if (!name.equals(luaType.name)) {
			return false;
		}
		return otherTypes.equals(luaType.otherTypes);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + otherTypes.hashCode();
	}
}
