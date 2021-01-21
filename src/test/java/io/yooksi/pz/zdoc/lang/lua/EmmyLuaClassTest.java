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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmmyLuaClassTest {

	@Test
	void shouldCorrectlyFormatEmmyLuaClassAnnotation() {

		// ---@class TYPE
		EmmyLuaClass annotation = new EmmyLuaClass("Car", null);
		Assertions.assertEquals("---@class Car", annotation.toString());

		// ---@class TYPE[:PARENT_TYPE]
		annotation = new EmmyLuaClass("Car", "Vehicle");
		Assertions.assertEquals("---@class Car : Vehicle", annotation.toString());

		// ---@class TYPE[:PARENT_TYPE] [@comment]
		annotation = new EmmyLuaClass("Car", "Vehicle", "goes vroom");
		Assertions.assertEquals("---@class Car : Vehicle @goes vroom", annotation.toString());
	}

	@Test
	void shouldParseEmmyLuaClassAnnotationFromString() {

		String[] classAnnotations = new String[]{
				"---@class type",
				"---@class type @comment_",
				"---@class type@ comment-line",
				"---@class type:other_type",
				"---@class type:other_type @comment",
				"---@class type : other_type @comment# !line",
				"---@class type: other_type@comment	",
				"---@class type :other_type @ comment  ",
		};
		for (String classAnnotation : classAnnotations) {
			Assertions.assertTrue(EmmyLuaClass.isAnnotation(classAnnotation));
		}
		Assertions.assertFalse(EmmyLuaClass.isAnnotation("--@class testClass"));
		Assertions.assertFalse(EmmyLuaClass.isAnnotation("---@classs testClass"));
	}
}
