/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
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
package io.yooksi.pz.zdoc.element.lua;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import io.yooksi.pz.zdoc.lang.lua.EmmyLuaClass;

class LuaMethodTest {

	private static final LuaParameter DUMMY_PARAM =
			new LuaParameter(new LuaType("dummy"), "param1");

	private static final LuaMethod TEST_METHOD = LuaMethod.Builder.create("test")
			.withOwner(new LuaClass("TestClass")).withParams(ImmutableList.of(
					DUMMY_PARAM, new LuaParameter(new LuaType("dummy"), "param2"))).build();

	@Test
	void shouldCreateLuaMethodWithSafeLuaName() {

		LuaType type = new LuaType("Boolean");
		List<LuaParameter> params = new ArrayList<>();

		LuaMethod method = LuaMethod.Builder.create("break").withReturnType(type).withParams(params).build();
		Assertions.assertEquals("_break", method.getName());

		method = LuaMethod.Builder.create("test").withReturnType(type).withParams(params).build();
		Assertions.assertNotEquals("_test", method.getName());
	}

	@Test
	void shouldCorrectlyConvertLuaMethodToString() {

		Assertions.assertEquals("TestClass:test(param1, param2)", TEST_METHOD.toString());

		List<LuaParameter> params = ImmutableList.of(
				new LuaParameter(new LuaType("String"), "param1"),
				new LuaParameter(new LuaType("Integer"), "param2")
		);
		LuaMethod varArgMethod = LuaMethod.Builder.create("test").withOwner(new LuaClass("TestClass"))
				.withReturnType(new LuaType("Object")).withParams(params).withVarArg(true).build();

		Assertions.assertEquals("TestClass:test(param1, ...)", varArgMethod.toString());
	}

	@Test
	void shouldIgnoreLuaMethodHasVarArgWhenEmptyParamList() {

		Assertions.assertFalse(LuaMethod.Builder.create("test")
				.withReturnType(new LuaType("Boolean")).withVarArg(true).build().hasVarArg()
		);
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingLuaMethodAnnotations() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> TEST_METHOD.getAnnotations().add(new EmmyLuaClass("TestType", null)));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingLuaMethodParameters() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> TEST_METHOD.getParams().add(DUMMY_PARAM));
	}
}
