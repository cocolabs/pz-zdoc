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
package io.yooksi.pz.zdoc.lang.lua;

import java.util.regex.Pattern;

import io.yooksi.pz.zdoc.element.lua.LuaType;

/**
 * Use {@code @return} to specify the return type of a function
 * <ul>
 * <li>Full format:
 * <pre>
 * ---@return MY_TYPE[|OTHER_TYPE] [@comment]
 * </pre></li>
 * <li>Target:
 * <ul style="list-style-type:circle">
 * <li>functions<pre>
 * ---@return Car|Ship
 * local function create()
 *     ...
 * end
 * ---Here car_or_ship doesn't need @type annotation,
 * ---EmmyLua has already inferred the type via "create" function
 * local car_or_ship = create()
 * </pre>
 * <pre>
 * ---@return Car
 * function factory:create()
 *     ...
 * end
 * </pre></li>
 * </ul></li>
 * </ul>
 *
 * @see <a href="https://git.io/JLPlm">EmmyLua Documentation</a>
 */
public class EmmyLuaReturn extends EmmyLua {

	private static final Pattern REGEX = Pattern.compile(
			"^---\\s*@return\\s+(\\w+)(?:\\s*\\|\\s*(\\w+))?(?:\\s*@\\s*(.*))?\\s*$"
	);

	public EmmyLuaReturn(LuaType type, String comment) {
		super("return", formatType(type), comment);
	}

	public EmmyLuaReturn(LuaType type) {
		this(type, "");
	}

	public static boolean isAnnotation(String text) {
		return REGEX.matcher(text).find();
	}
}
