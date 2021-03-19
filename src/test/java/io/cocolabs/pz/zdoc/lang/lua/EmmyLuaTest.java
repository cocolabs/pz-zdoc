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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.cocolabs.pz.zdoc.MainTest;
import io.cocolabs.pz.zdoc.element.lua.LuaType;

class EmmyLuaTest {

	@Test
	void shouldCorrectlyValidateEmmyLuaBuiltInTypes() {

		for (String type : EmmyLua.BUILT_IN_TYPES)
		{
			Assertions.assertTrue(EmmyLua.isBuiltInType(type));
			Assertions.assertFalse(EmmyLua.isBuiltInType(type.toUpperCase()));
		}
		Assertions.assertFalse(EmmyLua.isBuiltInType("testType"));
	}

	@Test
	void shouldCorrectlyValidateLuaReservedKeywords() {

		for (String keyword : EmmyLua.RESERVED_KEYWORDS)
		{
			Assertions.assertTrue(EmmyLua.isReservedKeyword(keyword));
			Assertions.assertFalse(EmmyLua.isReservedKeyword(keyword.toUpperCase()));
		}
		Assertions.assertFalse(EmmyLua.isReservedKeyword("testKeyword"));
	}

	@Test
	void shouldEnsureLuaMemberNameSafety() {

		Set<String> reserved = new HashSet<>();
		reserved.addAll(EmmyLua.RESERVED_KEYWORDS);
		reserved.addAll(EmmyLua.BUILT_IN_TYPES);

		for (String keyword : reserved) {
			Assertions.assertEquals('_' + keyword, EmmyLua.getSafeLuaName(keyword));
		}
		Assertions.assertNotEquals("type", EmmyLua.getSafeLuaName("Type"));
	}

	@Test
	void shouldFormatTypeWithCorrectSafeName() {

		MainTest.registerClassOverride("Vector2", "JVector2");
		MainTest.registerClassOverride("TestClass", "JTestClass");

		LuaType expectedResult = new LuaType("Vector2");
		Assertions.assertEquals("JVector2", EmmyLua.formatType(expectedResult));

		expectedResult = new LuaType("Vector2", new LuaType("TestClass"));
		Assertions.assertEquals("JVector2|JTestClass", EmmyLua.formatType(expectedResult));
	}
}
