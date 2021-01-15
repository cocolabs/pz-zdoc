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
package io.yooksi.pz.zdoc.element.lua;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaClass;

class LuaMethodTest implements UnitTest {

	private static final LuaParameter DUMMY_PARAM =
			new LuaParameter(new LuaType("dummy"), "param1");

	private static final LuaMethod TEST_METHOD = new LuaMethod("test",
			new LuaClass("TestClass"), MemberModifier.UNDECLARED, new LuaType("void"),
			ImmutableList.of(DUMMY_PARAM, new LuaParameter(new LuaType("dummy"), "param2"))
	);

	@Test
	void shouldCreateLuaMethodWithSafeLuaName() {

		LuaType type = new LuaType("Boolean");
		List<LuaParameter> params = new ArrayList<>();

		LuaMethod method = new LuaMethod("break", MemberModifier.UNDECLARED, type, params);
		Assertions.assertEquals("_break", method.getName());

		method = new LuaMethod("test", MemberModifier.UNDECLARED, type, params);
		Assertions.assertNotEquals("_test", method.getName());
	}

	@Test
	void shouldCorrectlyConvertLuaMethodToString() {
		Assertions.assertEquals("TestClass:test(param1, param2)", TEST_METHOD.toString());
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingLuaMethodAnnotations() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> TEST_METHOD.getAnnotations().add(new EmmyLuaClass(new LuaClass("TestType"))));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingLuaMethodParameters() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> TEST_METHOD.getParams().add(DUMMY_PARAM));
	}
}
