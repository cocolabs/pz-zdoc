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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.cocolabs.pz.zdoc.element.lua.LuaMethod;
import io.cocolabs.pz.zdoc.element.lua.LuaType;

class EmmyLuaReturnTest {

	@Test
	void shouldCorrectlyFormatEmmyLuaReturnAnnotation() {

		// ---@return TYPE
		EmmyLuaReturn annotation = new EmmyLuaReturn(new LuaMethod.ReturnType("Dog"));
		Assertions.assertEquals("---@return Dog", annotation.toString());

		// ---@return TYPE @comment
		LuaType luaType = new LuaType("Dog");
		String comment = "returns a dog class";

		annotation = new EmmyLuaReturn(new LuaMethod.ReturnType(luaType, comment));
		Assertions.assertEquals("---@return Dog @" + comment, annotation.toString());

		// ---@return TYPE|OTHER_TYPE @commnet
		luaType = new LuaType("Dog", new LuaType("Animal"));
		comment = "returns an Animal class";

		annotation = new EmmyLuaReturn(new LuaMethod.ReturnType(luaType, comment));
		Assertions.assertEquals("---@return Dog|Animal @" + comment, annotation.toString());
	}

	@Test
	void shouldParseEmmyLuaReturnAnnotationFromString() {

		String[] returnAnnotations = new String[]{
				"---@return type",
				"---@return type @comment_",
				"---@return type@ comment-line",
				"---@return type|other_type",
				"---@return type|other_type @comment",
				"---@return type | other_type @comment# !line",
				"---@return type| other_type@comment	",
				"---@return type |other_type @ comment  ",
		};
		for (String returnAnnotation : returnAnnotations) {
			Assertions.assertTrue(EmmyLuaReturn.isAnnotation(returnAnnotation));
		}
		Assertions.assertFalse(EmmyLuaReturn.isAnnotation("--@return testReturnType"));
		Assertions.assertFalse(EmmyLuaReturn.isAnnotation("---@returnk testReturnType"));
	}
}
