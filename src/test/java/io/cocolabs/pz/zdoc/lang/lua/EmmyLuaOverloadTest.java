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

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.cocolabs.pz.zdoc.element.lua.LuaParameter;
import io.cocolabs.pz.zdoc.element.lua.LuaType;

public class EmmyLuaOverloadTest {

	@Test
	void shouldProperlyFormatEmmyLuaOverloadAnnotation() {

		List<LuaParameter> params = ImmutableList.of(
			new LuaParameter(new LuaType("Object"), "arg0"),
			new LuaParameter(new LuaType("Number"), "arg1"),
			new LuaParameter(new LuaType("String"), "arg2")
		);
		EmmyLuaOverload annotation = new EmmyLuaOverload(params);

		String expected = "---@overload fun(arg0:Object, arg1:Number, arg2:String)";
		Assertions.assertEquals(expected, annotation.toString());
	}
}
