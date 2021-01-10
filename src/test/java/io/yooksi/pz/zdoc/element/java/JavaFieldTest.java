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
package io.yooksi.pz.zdoc.element.java;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;

@SuppressWarnings("unused")
public class JavaFieldTest implements UnitTest {

	public Object publicField;
	private Object privateField;
	Object defaultField;

	private static Object staticField;
	private static final Object staticFinalField = null;

	@Test
	void shouldConstructJavaFieldWithValidNameFromFieldObject() {

		for (Field declaredField : JavaFieldTest.class.getDeclaredFields()) {
			Assertions.assertEquals(declaredField.getName(), new JavaField(declaredField).getName());
		}
	}

	@Test
	void shouldConstructJavaFieldWithValidNameFromClassObject() {

		for (Field declaredField : JavaFieldTest.class.getDeclaredFields()) {
			Assertions.assertEquals(declaredField.getName(), new JavaField(declaredField.getType(),
					declaredField.getName(), new MemberModifier(declaredField.getModifiers())).getName());
		}
	}

	@Test
	void shouldConstructJavaFieldWithValidReadableForm() {

		String[] fieldData = new String[]{
				"publicField", "public java.lang.Object",
				"privateField", "private java.lang.Object",
				"defaultField", "java.lang.Object",
				"staticField", "private static java.lang.Object",
				"staticFinalField", "private static final java.lang.Object"
		};
		for (int i = 0; i < fieldData.length; i += 2)
		{
			String expected = fieldData[i + 1] + ' ' + fieldData[i];

			Field declaredField = getDeclaredField(fieldData[i]);
			Assertions.assertEquals(expected, new JavaField(declaredField).toString());

			try {
				String[] elements = fieldData[i + 1].split("\\s+");
				Class<?> clazz = Class.forName(elements[elements.length - 1]);
				MemberModifier modifier = new MemberModifier(declaredField.getModifiers());
				Assertions.assertEquals(expected, new JavaField(clazz, fieldData[i], modifier).toString());
			}
			catch(ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@TestOnly
	private static Field getDeclaredField(String name) {

		return Arrays.stream(JavaFieldTest.class.getDeclaredFields())
				.filter(f -> f.getName().equals(name)).findFirst()
				.orElseThrow(RuntimeException::new);
	}
}
