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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.element.lua.LuaType;

class EmmyLuaParamTest {

	@Test
	void shouldCorrectlyFormatEmmyLuaParamAnnotation() {

		// ---@param param_name TYPE
		EmmyLuaParam annotation = new EmmyLuaParam("apple", new LuaType("Apple"));
		Assertions.assertEquals("---@param apple Apple", annotation.toString());

		// ---@param param_name TYPE[|other_type]
		annotation = new EmmyLuaParam("apple",
				new LuaType("Apple", new LuaType("Fruit"))
		);
		Assertions.assertEquals("---@param apple Apple|Fruit", annotation.toString());

		// ---@param param_name TYPE[|other_type] [@comment]
		annotation = new EmmyLuaParam("apple", new LuaType("Apple",
				new LuaType("Fruit")), "very healthy"
		);
		Assertions.assertEquals("---@param apple Apple|Fruit @very healthy", annotation.toString());
	}

	@Test
	void shouldParseEmmyLuaParamAnnotationFromString() {

		String[] paramAnnotations = new String[]{
				"---@param param_name type",
				"---@param param_name type @comment_",
				"---@param param_name type@ comment-line",
				"---@param param_name type|other_type",
				"---@param param_name type|other_type @comment",
				"---@param param_name type | other_type @comment# !line",
				"---@param param_name type| other_type@comment	",
				"---@param param_name type |other_type @ comment  ",
		};
		for (String paramAnnotation : paramAnnotations) {
			Assertions.assertTrue(EmmyLuaParam.isAnnotation(paramAnnotation));
		}
		Assertions.assertFalse(EmmyLuaParam.isAnnotation("--@param testParam testType"));
		Assertions.assertFalse(EmmyLuaClass.isAnnotation("---@params testParam testType"));
	}
}
