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
package io.yooksi.pz.zdoc.element.lua;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import io.yooksi.pz.zdoc.element.IField;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaField;

public class LuaField implements IField, Annotated {

	private final String name;
	private final LuaType type;
	private final MemberModifier modifier;

	private final List<EmmyLua> annotations;

	public LuaField(LuaType type, String name, MemberModifier modifier) {
		this.type = type;
		this.name = EmmyLua.getSafeLuaName(name);
		this.modifier = modifier;
		if (!modifier.hasAccess(AccessModifierKey.DEFAULT))
		{
			this.annotations = Collections.singletonList(
					new EmmyLuaField(name, modifier.getAccess().name, type));
		}
		else this.annotations = Collections.singletonList(new EmmyLuaField(name, type));
	}

	@Override
	public String toString() {
		return (type.name + ' ' + name).trim();
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
	public MemberModifier getModifier() {
		return modifier;
	}

	@Override
	public String getComment() {
		return "";
	}

	@Override
	public @Unmodifiable List<EmmyLua> getAnnotations() {
		return annotations;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LuaField luaField = (LuaField) o;

		if (!name.equals(luaField.name)) {
			return false;
		}
		if (!type.equals(luaField.type)) {
			return false;
		}
		return modifier.equals(luaField.modifier);
	}

	@Override
	public int hashCode() {

		int result = 31 * name.hashCode() + type.hashCode();
		result = 31 * result + type.hashCode();
		return 31 * result + modifier.hashCode();
	}
}
