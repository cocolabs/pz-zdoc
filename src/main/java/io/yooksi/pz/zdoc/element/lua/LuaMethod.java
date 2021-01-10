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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaAccess;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.element.IMethod;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaReturn;

/**
 * This class represents a parsed Lua method.
 */
@SuppressWarnings("unused")
public class LuaMethod implements IMethod, Annotated {

	private final @Nullable LuaClass owner;

	private final String name;
	private final LuaType returnType;
	private final List<LuaParameter> params;
	private final MemberModifier modifier;
	private final List<EmmyLua> annotations;

	public LuaMethod(String name, @Nullable LuaClass owner, MemberModifier modifier,
					 LuaType returnType, List<LuaParameter> params) {

		this.name = EmmyLua.getSafeLuaName(name);
		this.owner = owner;
		this.returnType = returnType;
		this.params = Collections.unmodifiableList(params);
		this.modifier = modifier;

		List<EmmyLua> annotations = new ArrayList<>();
		if (!modifier.hasAccess(AccessModifierKey.DEFAULT)) {
			annotations.add(new EmmyLuaAccess(modifier.getAccess()));
		}
		params.forEach(p -> annotations.addAll(p.getAnnotations()));
		annotations.add(new EmmyLuaReturn(returnType));
		this.annotations = Collections.unmodifiableList(annotations);
	}

	public LuaMethod(String name, MemberModifier modifier, LuaType returnType, List<LuaParameter> params) {
		this(name, null, modifier, returnType, params);
	}

	@Override
	public String toString() {

		List<String> params = new ArrayList<>();
		this.params.forEach(p -> params.add(p.getName()));

		return String.format("%s%s(%s)", owner != null ? owner.getName() + ':' : "",
				getName(), StringUtils.join(params, ','));
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
	public LuaType getReturnType() {
		return returnType;
	}

	@Override
	public @UnmodifiableView List<LuaParameter> getParams() {
		return params;
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
		LuaMethod luaMethod = (LuaMethod) o;

		if (!Objects.equals(owner, luaMethod.owner)) {
			return false;
		}
		if (!name.equals(luaMethod.name)) {
			return false;
		}
		if (!returnType.equals(luaMethod.returnType)) {
			return false;
		}
		if (!params.equals(luaMethod.params)) {
			return false;
		}
		return modifier.equals(luaMethod.modifier);
	}

	@Override
	public int hashCode() {

		int result = owner != null ? owner.hashCode() : 0;
		result = 31 * result + name.hashCode();
		result = 31 * result + returnType.hashCode();
		result = 31 * result + params.hashCode();
		return 31 * result + modifier.hashCode();
	}
}
