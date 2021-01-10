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
package io.yooksi.pz.zdoc.lang.lua;

import io.yooksi.pz.zdoc.element.lua.LuaType;

import java.util.regex.Pattern;

/**
 * Use {@code @param} to specify the types of function parameters,
 * to improve completions and other functionality.
 * <ul>
 * <li>Full format:
 * <pre>
 * 	--@param param_name MY_TYPE[|other_type] [@comment]
 * </pre>
 * </li>
 * <li>Target:
 * <ul style="list-style-type:circle">
 * <li>function parameters<pre>
 * ---@param car Car
 * local function setCar(car)
 *     ...
 * end
 * </pre>
 * <pre>
 * ---@param car Car
 * setCallback(function(car)
 *     ...
 * end)
 * </pre></li>
 * <li>for loop variables<pre>
 * ---@param car Car
 * for k, car in ipairs(list) do
 * end
 * </pre></li>
 * </ul></li>
 * </ul>
 *
 * @see <a href="https://git.io/JLPlL">EmmyLua Documentation</a>
 */
public class EmmyLuaParam extends EmmyLua {

	private static final Pattern REGEX = Pattern.compile(
			"^---\\s*@param\\s+(\\w+)\\s+(\\w+)(?:\\s*\\|\\s*(\\w+))?(?:\\s*@\\s*(.*))?\\s*$"
	);

	public EmmyLuaParam(String name, LuaType type, String comment) {
		super("param", String.format("%s %s", name, formatType(type)), comment);
	}

	public EmmyLuaParam(String name, LuaType type) {
		this(name, type, "");
	}

	public static boolean isAnnotation(String text) {
		return REGEX.matcher(text).find();
	}
}
