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
package io.yooksi.pz.zdoc.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;

public class EmmyLuaTest implements UnitTest {

	@Test
	void shouldCorrectlyFormatEmmyLuaClassAnnotation() {

		// ---@class TYPE
		String annotation = EmmyLua.CLASS.create(new String[]{ "Car" });
		Assertions.assertEquals("---@class Car", annotation);

		// ---@class TYPE[:PARENT_TYPE]
		annotation = EmmyLua.CLASS.create(new String[]{ "Car", "Vehicle" });
		Assertions.assertEquals("---@class Car : Vehicle", annotation);

		// ---@class TYPE[:PARENT_TYPE] [@comment]
		annotation = EmmyLua.CLASS.create(new String[]{ "Car", "Vehicle", "goes vroom" });
		Assertions.assertEquals("---@class Car : Vehicle @goes vroom", annotation);
	}

	@Test
	void shouldCorrectlyFormatEmmyLuaParamAnnotation() {

		// ---@param param_name
		String annotation = EmmyLua.PARAM.create(new String[]{ "apple" });
		Assertions.assertEquals("---@param apple", annotation);

		// ---@param param_name TYPE
		annotation = EmmyLua.PARAM.create(new String[]{ "apple", "Apple" });
		Assertions.assertEquals("---@param apple Apple", annotation);

		// ---@param param_name TYPE[|other_type]
		annotation = EmmyLua.PARAM.create(new String[]{ "apple", "Apple", "Fruit" });
		Assertions.assertEquals("---@param apple Apple|Fruit", annotation);

		// ---@param param_name TYPE[|other_type] [@comment]
		annotation = EmmyLua.PARAM.create(new String[]{ "apple", "Apple", "Fruit", "very healthy" });
		Assertions.assertEquals("---@param apple Apple|Fruit @very healthy", annotation);
	}

	@Test
	void shouldCorrectlyFormatEmmyLuaReturnAnnotation() {

		// ---@param TYPE
		String annotation = EmmyLua.RETURN.create(new String[]{ "Dog" });
		Assertions.assertEquals("---@return Dog", annotation);

		// ---@param TYPE|OTHER_TYPE
		annotation = EmmyLua.RETURN.create(new String[]{ "Dog", "Animal" });
		Assertions.assertEquals("---@return Dog|Animal", annotation);
	}
}
