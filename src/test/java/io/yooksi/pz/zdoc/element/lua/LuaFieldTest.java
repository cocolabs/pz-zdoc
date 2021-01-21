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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaClass;

class LuaFieldTest implements UnitTest {

	@Test
	void shouldCreateLuaFieldWithSafeLuaName() {

		Assertions.assertEquals("_break", new LuaField(new LuaType("Boolean"),
				"break", MemberModifier.UNDECLARED).getName());

		Assertions.assertNotEquals("_test", new LuaField(new LuaType("Boolean"),
				"test", MemberModifier.UNDECLARED).getName());
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingLuaFieldAnnotations() {

		LuaField field = new LuaField(new LuaType("test"), "type", MemberModifier.UNDECLARED);
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> field.getAnnotations().add(new EmmyLuaClass("TestType", null)));
	}
}
