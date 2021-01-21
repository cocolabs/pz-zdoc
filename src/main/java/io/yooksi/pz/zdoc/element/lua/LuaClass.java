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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import io.yooksi.pz.zdoc.element.IClass;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaClass;

/**
 * This class represents a parsed lua class reference.
 */
public class LuaClass implements IClass, Annotated {

	private final String type;
	private final String conventional;
	private final @Nullable String parentType;
	private final List<EmmyLua> annotations;

	public LuaClass(String type, @Nullable String parentType) {

		this.type = type;
		this.conventional = getConventionalName(type);
		/*
		 * ensure that parent type is different then base type, this check
		 * can be handled elsewhere but we should do it here to ensure safety
		 */
		this.parentType = !type.equals(parentType) ? parentType : null;
		this.annotations = Collections.singletonList(new EmmyLuaClass(type, getParentType()));
	}

	public LuaClass(String type) {
		this(type, null);
	}

	/**
	 * {@code LuaClass} type names follow official Lua lexical conventions:
	 * <blockquote>
	 * Names (also called identifiers) in Lua can be any string of Latin letters,
	 * Arabic-Indic digits, and underscores, not beginning with a digit and not being a
	 * reserved word. Identifiers are used to name variables, table fields, and labels.
	 * </blockquote>
	 * @see <a href="https://www.lua.org/manual/5.4/manual.html#3.1">Lua Lexical Conventions</a>
	 */
	private static String getConventionalName(String name) {

		String result = name;
		if (result.contains(".")) {
			result = result.replace('.', '_');
		}
		if (Character.isDigit(result.charAt(0))) {
			result = '_' + result.substring(1);
		}
		return EmmyLua.getSafeLuaName(result);
	}

	public @Nullable String getParentType() {
		return parentType;
	}

	@Override
	public String getName() {
		return type;
	}

	public String getConventionalName() {
		return conventional;
	}

	@Override
	public List<IClass> getTypeParameters() {
		return new ArrayList<>();
	}

	@Override
	public String toString() {
		return parentType != null ? type + " : " + parentType : type;
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
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		LuaClass luaClass = (LuaClass) obj;

		if (!type.equals(luaClass.type)) {
			return false;
		}
		return Objects.equals(parentType, luaClass.parentType);
	}

	@Override
	public int hashCode() {
		return 31 * type.hashCode() + (parentType != null ? parentType.hashCode() : 0);
	}
}
