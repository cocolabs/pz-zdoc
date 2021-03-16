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

import io.cocolabs.pz.zdoc.element.lua.LuaParameter;

/**
 * Use {@code @overload} to specify overloaded methods.
 * <ul>
 * <li>Full format:
 * <pre>
 * 	---{@code @overloaded} fun(param_name:MY_TYPE[|other_type]...) [@comment]
 * </pre>
 * </li>
 * </ul>
 *
 * @see <a href="https://git.io/JqhaG">EmmyLua Documentation</a>
 */
public class EmmyLuaOverload extends EmmyLua {

	public EmmyLuaOverload(List<LuaParameter> params, String comment) {
		super("overload", formatAnnotation(params), comment);
	}

	private static String formatAnnotation(List<LuaParameter> params) {

		StringBuilder sb = new StringBuilder();
		if (!params.isEmpty())
		{
			LuaParameter param = params.get(0);
			sb.append(param.getName()).append(':').append(formatType(param.getType()));
		}
		for (int i = 1; i < params.size(); i++)
		{
			LuaParameter param = params.get(i);
			sb.append(", ").append(param.getName()).append(':').append(formatType(param.getType()));
		}
		return String.format("fun(%s)", sb.toString());
	}
}
