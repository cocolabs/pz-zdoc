/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2021 Matthew Cain
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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.element.lua.LuaType;

public class EmmyLuaFieldTest {

	@Test
	void shouldCorrectlyFormatEmmyLuaFieldAnnotation() {

		// ---@field field_name FIELD_TYPE
		EmmyLuaField annotation = new EmmyLuaField("car", new LuaType("Vehicle"));
		Assertions.assertEquals("---@field car Vehicle", annotation.toString());

		// ---@field field_name FIELD_TYPE|OTHER_TYPE
		annotation = new EmmyLuaField("car", new LuaType("Vehicle",
				List.of(new LuaType("Object"))
		));
		Assertions.assertEquals("---@field car Vehicle|Object", annotation.toString());

		// ---@field public field_name FIELD_TYPE|OTHER_TYPE
		annotation = new EmmyLuaField("car", "public", new LuaType("Vehicle",
				List.of(new LuaType("Object"))
		));
		Assertions.assertEquals("---@field public car Vehicle|Object", annotation.toString());

		// ---@field field_name FIELD_TYPE|OTHER_TYPE [@comment]
		annotation = new EmmyLuaField("car", new LuaType("Vehicle",
				List.of(new LuaType("Object"))), "goes vroom"
		);
		Assertions.assertEquals("---@field car Vehicle|Object @goes vroom", annotation.toString());

		// ---@field protected field_name FIELD_TYPE|OTHER_TYPE [@comment]
		annotation = new EmmyLuaField("car", "protected", new LuaType("Vehicle",
				List.of(new LuaType("Object"))), "goes vroom"
		);
		Assertions.assertEquals("---@field protected car Vehicle|Object @goes vroom", annotation.toString());
	}

	@Test
	void shouldParseEmmyLuaFieldAnnotationFromString() {

		String[] fieldAnnotations = new String[]{
				"---@field type",
				"---@field public type",
				"---@field type @comment_",
				"---@field protected type @comment_",
				"---@field type@ comment-line",
				"---@field private type@ comment-line",
				"---@field type|other_type",
				"---@field public type|other_type",
				"---@field type|other_type @comment",
				"---@field protected type|other_type @comment",
				"---@field type | other_type @comment# !line",
				"---@field private type | other_type @comment# !line",
				"---@field type| other_type@comment	",
				"---@field public type| other_type@comment	",
				"---@field type |other_type @ comment  ",
				"---@field protected type |other_type @ comment  ",
		};
		for (String fieldAnnotation : fieldAnnotations) {
			Assertions.assertTrue(EmmyLuaField.isAnnotation(fieldAnnotation));
		}
		Assertions.assertFalse(EmmyLuaField.isAnnotation("--@field testClass"));
		Assertions.assertFalse(EmmyLuaField.isAnnotation("---@fields testClass"));
	}
}
