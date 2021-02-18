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

import io.cocolabs.pz.zdoc.element.lua.LuaType;

/**
 * Used to denote a variadic argument.
 * <ul>
 * <li>Full format:
 * <pre>
 * ---@vararg TYPE
 * </pre></li>
 * <li>Example:
 * <pre>
 * ---@vararg string
 * ---@return string
 * local function format(...)
 *     local tbl = { ... } -- inferred as string[]
 * end
 * </pre>
 * </li>
 * </ul>
 *
 * @see <a href="https://git.io/JLPlm">EmmyLua Documentation</a>
 */
public class EmmyLuaVarArg extends EmmyLua {

	public EmmyLuaVarArg(LuaType type) {
		super("vararg", type.getName(), "");
	}
}
