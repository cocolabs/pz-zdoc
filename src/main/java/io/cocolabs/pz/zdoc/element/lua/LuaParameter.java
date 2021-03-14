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
package io.cocolabs.pz.zdoc.element.lua;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import io.cocolabs.pz.zdoc.element.IParameter;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLua;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLuaParam;

public class LuaParameter implements IParameter, Annotated {

	private final LuaType type;
	private final String name, comment;
	private final List<EmmyLua> annotations;

	public LuaParameter(LuaType type, String name, String comment) {

		this.type = type;
		this.name = EmmyLua.getSafeLuaName(name);
		this.comment = comment;
		this.annotations = Collections.singletonList(new EmmyLuaParam(this.name, type));
	}

	public LuaParameter(LuaType type, String name) {
		this(type, name, "");
	}

	@Override
	public String toString() {
		return (type.getName() + ' ' + name).trim();
	}

	@Override
	public String getAsVarArg() {
		return "...";
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public LuaType getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public @Unmodifiable List<EmmyLua> getAnnotations() {
		return annotations;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LuaParameter)) {
			return false;
		}
		LuaParameter that = (LuaParameter) obj;

		if (!type.equals(that.type)) {
			return false;
		}
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return 31 * type.hashCode() + name.hashCode();
	}
}
