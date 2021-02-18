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
package io.cocolabs.pz.zdoc.lang.lua;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.cocolabs.pz.zdoc.element.lua.LuaType;

/**
 * This class represents an EmmyLua annotation
 *
 * @see <a href="https://emmylua.github.io/">EmmyLua for IntelliJ IDEA</a>
 */
public abstract class EmmyLua {

	/**
	 * Set of keywords reserved by EmmyLua plugin.
	 *
	 * @see <a href="https://git.io/JLPWh">IntelliJ-EmmyLua - builtin.lua</a>
	 */
	static final Set<String> BUILT_IN_TYPES = Sets.newHashSet(
			"boolean", "string", "number", "userdata",
			"thread", "table", "any", "void", "self"
	);

	/**
	 * Set of keywords reserved by Lua language.
	 *
	 * @see <a href="https://www.lua.org/manual/5.1/manual.html#2.1">
	 * 		Lexical Conventions - Lua 5.1 Reference Manual</a>
	 */
	static final Set<String> RESERVED_KEYWORDS = Sets.newHashSet(
			"and", "break", "do", "else", "elseif", "end",
			"false", "for", "function", "goto", "if", "in",
			"local", "nil", "not", "or", "repeat", "return",
			"then", "true", "until", "while"
	);

	private final String annotation;

	EmmyLua(String keyword, String annotation, String comment) {
		if (Strings.nullToEmpty(comment).trim().isEmpty()) {
			this.annotation = String.format("---@%s %s", keyword, annotation);
		}
		else this.annotation = String.format("---@%s %s @%s", keyword, annotation, comment);
	}

	EmmyLua(String keyword) {
		Validate.notEmpty(keyword);
		this.annotation = "---@" + keyword;
	}

	/**
	 * Returns {@code true} if given {@code String} is a keyword reserved by EmmyLua.
	 */
	public static boolean isBuiltInType(String type) {
		return BUILT_IN_TYPES.contains(type);
	}

	/**
	 * Returns {@code true} if given {@code String} is a keyword reserved by Lua.
	 */
	public static boolean isReservedKeyword(String keyword) {
		return RESERVED_KEYWORDS.contains(keyword);
	}

	/**
	 * Returns safe to use Lua member {@code String}.
	 *
	 * @return {@code String} that is safe to use as member name in Lua.
	 * 		If the given string matches (non-case-sensitive) a reserved or built-in Lua keyword,
	 * 		the result will be prepended with {@code '_'} character to avoid keyword clashing.
	 */
	public static String getSafeLuaName(String name) {
		return isReservedKeyword(name) || isBuiltInType(name) ? '_' + name : name;
	}

	static String formatType(LuaType type) {

		String typeName = type.getName();
		List<LuaType> typeParameters = type.getTypeParameters();
		if (!typeParameters.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			sb.append(typeName).append('|').append(readLuaTypeName(typeParameters.get(0)));
			for (int i = 1; i < typeParameters.size(); i++) {
				sb.append('|').append(readLuaTypeName(typeParameters.get(i)));
			}
			return sb.toString();
		}
		else return typeName;
	}

	private static String readLuaTypeName(@Nullable LuaType luaType) {
		return luaType != null ? luaType.getName() : "any";
	}

	/** @return textual representation of this annotation. */
	@Override
	public String toString() {
		return annotation;
	}
}
