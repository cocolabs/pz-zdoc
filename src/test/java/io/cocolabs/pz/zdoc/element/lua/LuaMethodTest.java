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
package io.cocolabs.pz.zdoc.element.lua;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.cocolabs.pz.zdoc.lang.lua.EmmyLua;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLuaClass;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLuaOverload;

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
	void shouldCreateMethodWithOverloadProperties() {

		Set<LuaMethod> overloadMethods = Sets.newHashSet(
				LuaMethod.Builder.create("testMethod").withParams(
						new LuaParameter(new LuaType("Object"), "arg0")
				).build(),
				LuaMethod.Builder.create("testMethod").withParams(
						new LuaParameter(new LuaType("Object"), "arg0"),
						new LuaParameter(new LuaType("Number"), "arg1")
				).build(),
				LuaMethod.Builder.create("testMethod").withParams(
						new LuaParameter(new LuaType("Object"), "arg0"),
						new LuaParameter(new LuaType("Number"), "arg1"),
						new LuaParameter(new LuaType("String"), "arg2")
				).build()
		);
		LuaMethod method = LuaMethod.Builder.create("testMethod").withOverloads(overloadMethods).build();

		List<EmmyLua> annotations = method.getAnnotations();
		Set<EmmyLuaOverload> overloadAnnotations = new HashSet<>();
		annotations.stream().filter(a -> a instanceof EmmyLuaOverload)
				.forEach(a -> overloadAnnotations.add((EmmyLuaOverload) a));

		Assertions.assertEquals(overloadMethods.size(), overloadAnnotations.size());
		for (LuaMethod overloadMethod : overloadMethods)
		{
			EmmyLuaOverload annotation = new EmmyLuaOverload(overloadMethod.getParams());
			Assertions.assertEquals(1, annotations.stream()
					.filter(a -> a.toString().equals(annotation.toString())).count());
		}
	}

	@Test
	void shouldCompareOverloadMethodsAccordingToParamSize() {

		Comparator<LuaMethod> comparator = new LuaMethod.OverloadMethodComparator();

		// identical to luaMethods[1] array entry
		LuaMethod luaMethod = LuaMethod.Builder.create("method").withParams(
				new LuaParameter(new LuaType("Object"), "arg0"),
				new LuaParameter(new LuaType("String"), "arg1")
		).build();

		LuaMethod[] luaMethods = new LuaMethod[] {
			LuaMethod.Builder.create("method").withParams(
					new LuaParameter(new LuaType("Object"), "arg0")
			).build(),
			LuaMethod.Builder.create("method").withParams(
					new LuaParameter(new LuaType("Object"), "arg0"),
					new LuaParameter(new LuaType("String"), "arg1")
			).build(),
			LuaMethod.Builder.create("method").withParams(
					new LuaParameter(new LuaType("Object"), "arg0"),
					new LuaParameter(new LuaType("Number"), "arg1"),
					new LuaParameter(new LuaType("Number"), "arg2")
			).build(),
			LuaMethod.Builder.create("method").withParams(
					new LuaParameter(new LuaType("Object"), "arg0"),
					new LuaParameter(new LuaType("String"), "arg2"),
					new LuaParameter(new LuaType("Number"), "arg3"),
					new LuaParameter(new LuaType("Object"), "arg4")
			).build()
		};
		Assertions.assertEquals(-1, comparator.compare(luaMethods[0], luaMethods[1]));
		Assertions.assertEquals(-1, comparator.compare(luaMethods[1], luaMethods[2]));
		Assertions.assertEquals(0, comparator.compare(luaMethods[1], luaMethod));
		Assertions.assertEquals(1, comparator.compare(luaMethods[3], luaMethods[2]));

		SortedSet<LuaMethod> sortedSet = new TreeSet<>(comparator);
		sortedSet.addAll(Arrays.asList(luaMethods));

		Iterator<LuaMethod> iter = sortedSet.iterator();
		for (int i = 0; i < sortedSet.size(); i++) {
			Assertions.assertEquals(luaMethods[i], iter.next());
		}
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
