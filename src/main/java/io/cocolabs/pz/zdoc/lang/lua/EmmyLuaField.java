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

import java.util.regex.Pattern;

import io.cocolabs.pz.zdoc.element.lua.LuaType;

/**
 * Use {@code @field} to add extra fields to an existing class
 * (even if it doesn't appear in your code).
 * <ul>
 * <li>Full format:<pre>
 * ---@field [public|protected|private] field_name FIELD_TYPE[|OTHER_TYPE] [@comment]
 * </pre></li>
 * <li>Target:
 * <ul style="list-style-type:circle">
 * <li>After {@code @class} annotations<pre>
 * ---@class Car
 * ---@field public name string @add name field to class Car, you'll see it in code completion
 * local cls = class()
 * </pre></li>
 * </ul></li>
 * </ul>
 *
 * @see <a href="https://git.io/JLP7D">EmmyLua Documentation</a>
 */
public class EmmyLuaField extends EmmyLua {

	private static final Pattern REGEX = Pattern.compile(
			"^---\\s*@field(?:\\s+(public|protected|private))?" +
					"\\s+(\\w+)(?:\\s*\\|\\s*(\\w+))?(?:\\s*@\\s*(.*))?\\s*$"
	);

	public EmmyLuaField(String name, String access, LuaType type, String comment) {
		super("field", String.format("%s %s %s", access, name, formatType(type)), comment);
	}

	public EmmyLuaField(String name, LuaType type, String comment) {
		super("field", String.format("%s %s", name, formatType(type)), comment);
	}

	public EmmyLuaField(String name, String access, LuaType type) {
		this(name, access, type, "");
	}

	public EmmyLuaField(String name, LuaType type) {
		this(name, type, "");
	}

	public static boolean isAnnotation(String text) {
		return REGEX.matcher(text).find();
	}
}
