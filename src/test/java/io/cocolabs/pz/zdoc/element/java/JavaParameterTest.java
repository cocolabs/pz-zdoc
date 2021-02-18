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
package io.cocolabs.pz.zdoc.element.java;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "unused", "EmptyMethod" })
class JavaParameterTest {

	@Test
	void shouldConstructValidJavaParameterFromParameterObject() throws NoSuchMethodException {

		JavaParameter javaParam = new JavaParameter(JavaParameterTest.class.getDeclaredMethod(
				"testMethodWithParameter", Integer.class).getParameters()[0]);

		String paramName = javaParam.getName();
		Assertions.assertTrue(paramName.equals("arg0") || paramName.equals("param"));
		Assertions.assertEquals("java.lang.Integer", javaParam.getType().toString());
	}

	@Test
	void shouldConstructValidJavaParameterFromClassObject() {

		JavaParameter javaParam = new JavaParameter(Object.class, "param");

		Assertions.assertEquals("java.lang.Object", javaParam.getType().getName());
		Assertions.assertEquals("param", javaParam.getName());
	}

	@Test
	void whenComparingJavaParameterWithEqualsShouldCompareInternalDetails() {

		JavaParameter javaParam = new JavaParameter(Object.class, "param");

		Assertions.assertEquals(javaParam, new JavaParameter(Object.class, "param"));
		Assertions.assertNotEquals(javaParam, new JavaParameter(String.class, "param"));
		Assertions.assertNotEquals(javaParam, new JavaParameter(Object.class, "param1"));
	}

	@TestOnly
	private void testMethodWithParameter(Integer param) {
	}
}
