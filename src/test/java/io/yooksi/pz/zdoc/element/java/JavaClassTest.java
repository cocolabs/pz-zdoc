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

import io.yooksi.pz.zdoc.UnitTest;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class JavaClassTest implements UnitTest {

	@Test
	void shouldParseValidJavaClassFromClassObject() {

		JavaClass jClass = new JavaClass(JavaClassTest.class);
		String classPath = "io/yooksi/pz/zdoc/element/java/JavaClassTest";

		Assertions.assertEquals(classPath, JavaClass.getPathForClass(JavaClassTest.class));
		Assertions.assertEquals(JavaClassTest.class.getName(), jClass.getName());
	}

	@Test
	void shouldParseValidJavaClassFromInnerClassObject() {

		JavaClass jClass = new JavaClass(InnerClass.class);
		String classPath = "io/yooksi/pz/zdoc/element/java/JavaClassTest.InnerClass";

		Assertions.assertEquals(classPath, JavaClass.getPathForClass(InnerClass.class));
		Assertions.assertEquals(InnerClass.class.getTypeName(), jClass.getName());
	}

	@Test
	void shouldReturnNullPathWhenJavaClassHasNoPackage() throws NoSuchMethodException {

		Method[] methods = new Method[] {
				JavaClassTest.class.getDeclaredMethod("methodWithVoidReturnType"),
				JavaClassTest.class.getDeclaredMethod("methodWithPrimitiveReturnType"),
				JavaClassTest.class.getDeclaredMethod("methodWithBooleanReturnType"),
		};
		for (Method method : methods) {
			Assertions.assertEquals("", JavaClass.getPathForClass(method.getReturnType()));
		}
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingJavaClassTypeParameterList() {

		JavaClass jClass = new JavaClass(String.class);
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> jClass.getTypeParameters().add(new JavaClass(Object.class)));
	}

	@Test
	void whenComparingJavaClassWithEqualsShouldCompareInternalDetails() {

		// compare classes with same clazz field objects
		Assertions.assertEquals(new JavaClass(Object.class), new JavaClass(Object.class));
		Assertions.assertNotEquals(new JavaClass(Object.class), new JavaClass(String.class));

		List<JavaClass> simpleTypeParameters = List.of(
				new JavaClass(String.class), new JavaClass(Boolean.class)
		);
		// compare classes with same type parameter objects
		JavaClass jClassWithSimpleTypeParameters = new JavaClass(Object.class, simpleTypeParameters);
		Assertions.assertEquals(
				jClassWithSimpleTypeParameters, new JavaClass(Object.class, simpleTypeParameters)
		);
		// compare classes with different type parameters
		Assertions.assertNotEquals(
				jClassWithSimpleTypeParameters, new JavaClass(Object.class, List.of(
						new JavaClass(String.class), new JavaClass(Integer.class)
				))
		);
		// compare classes with different number of type parameters
		Assertions.assertNotEquals(
				jClassWithSimpleTypeParameters,
				new JavaClass(Object.class, List.of(new JavaClass(String.class)))
		);
		Assertions.assertNotEquals(
				jClassWithSimpleTypeParameters, new JavaClass(Object.class)
		);
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingUnknownTypeParameterList() {

		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> JavaClass.getUnknownTypeParameterList(1).add(null));
	}

	@Test
	void shouldThrowExceptionWhenGettingUnknownTypeParameterListWithIllegalSize() {

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> JavaClass.getUnknownTypeParameterList(0));

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> JavaClass.getUnknownTypeParameterList(3));
	}

	@Test
	void shouldGetCorrectUnknownTypeParameterListForValidSize() {

		for (int i = 1; i < 3; i++)
		{
			List<JavaClass> typeParameterList = JavaClass.getUnknownTypeParameterList(i);
			Assertions.assertEquals(i, typeParameterList.size());
			typeParameterList.forEach(Assertions::assertNull);
		}
	}

	private static class InnerClass {}

	@TestOnly
	private static void methodWithVoidReturnType() {
	}

	@TestOnly
	private static int methodWithPrimitiveReturnType() {
		return 0;
	}

	@TestOnly
	private static boolean methodWithBooleanReturnType() {
		return true;
	}
}
