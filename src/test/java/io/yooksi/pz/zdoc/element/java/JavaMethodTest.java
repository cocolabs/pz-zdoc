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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;

@SuppressWarnings("unused")
class JavaMethodTest implements UnitTest {

	private static final List<JavaParameter> DUMMY_PARAMS =
			ImmutableList.of(new JavaParameter(Object.class, "param1"));

	private static final JavaMethod METHOD_WITH_PARAMETERS =
			new JavaMethod(getDeclaredMethod("testMethodWithParameters"));

	private static final JavaMethod METHOD_WITHOUT_PARAMETERS =
			new JavaMethod(getDeclaredMethod("testMethodWithoutParametersOrReturnType"));

	private static final JavaMethod METHOD_WITH_RETURN_TYPE =
			new JavaMethod(getDeclaredMethod("testMethodWithReturnType"));

	private static final JavaMethod METHOD_WITHOUT_RETURN_TYPE =
			new JavaMethod(getDeclaredMethod("testMethodWithoutParametersOrReturnType"));

	@TestOnly
	private static JavaMethod copyJavaMethod(JavaMethod method) {
		return new JavaMethod(method.getName(), method.getReturnType().getClazz(),
				method.getParams(), method.getModifier());
	}

	@TestOnly
	private static void assertReadableForm(String form, JavaMethod method) {

		Assertions.assertEquals(form, method.toString());
		Assertions.assertEquals(form, copyJavaMethod(method).toString());
	}

	@TestOnly
	private static Method getDeclaredMethod(String name) {

		return Arrays.stream(JavaMethodTest.class.getDeclaredMethods())
				.filter(m -> m.getName().equals(name)).findFirst()
				.orElseThrow(RuntimeException::new);
	}

	@Test
	void shouldCreateJavaMethodWithValidName() {

		String methodName = "testMethodWithParameters";
		Assertions.assertEquals(methodName, METHOD_WITH_PARAMETERS.getName());

		JavaMethod method = new JavaMethod(methodName, Void.class, DUMMY_PARAMS, MemberModifier.UNDECLARED);
		Assertions.assertEquals(methodName, method.getName());
	}

	@Test
	void shouldCreateJavaMethodWithValidReadableForm() {

		assertReadableForm("private void " +
				"testMethodWithParameters(int arg0, java.lang.String arg1)", METHOD_WITH_PARAMETERS);

		assertReadableForm("private void " +
				"testMethodWithoutParametersOrReturnType()", METHOD_WITHOUT_PARAMETERS);

		assertReadableForm("private java.lang.Integer " +
				"testMethodWithReturnType()", METHOD_WITH_RETURN_TYPE);
	}

	@Test
	void shouldCreateJavaMethodWithValidParameters() {

		JavaMethod[] methods = new JavaMethod[]{
				METHOD_WITH_PARAMETERS, copyJavaMethod(METHOD_WITH_PARAMETERS),
		};
		for (JavaMethod method : methods)
		{
			List<JavaParameter> params = method.getParams();

			Assertions.assertEquals(2, params.size());

			JavaParameter firstParam = params.get(0);
			JavaParameter secondParam = params.get(1);

			Assertions.assertEquals("int", firstParam.getType().toString());
			Assertions.assertEquals("arg0", firstParam.getName());

			Assertions.assertEquals("java.lang.String", secondParam.getType().getName());
			Assertions.assertEquals("arg1", secondParam.getName());
		}
		Assertions.assertEquals(0, METHOD_WITHOUT_PARAMETERS.getParams().size());
		Assertions.assertEquals(0, copyJavaMethod(METHOD_WITHOUT_PARAMETERS).getParams().size());
	}

	@Test
	void shouldCreateJavaMethodWithValidReturnType() {

		MultiValuedMap<JavaMethod, String> map = new HashSetValuedHashMap<>();
		map.put(METHOD_WITH_RETURN_TYPE, "java.lang.Integer");
		map.put(copyJavaMethod(METHOD_WITH_RETURN_TYPE), "java.lang.Integer");
		map.put(METHOD_WITHOUT_RETURN_TYPE, "void");
		map.put(copyJavaMethod(METHOD_WITHOUT_RETURN_TYPE), "void");

		for (Map.Entry<JavaMethod, String> entry : map.entries()) {
			Assertions.assertEquals(entry.getValue(), entry.getKey().getReturnType().getName());
		}
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingJavaMethodParameters() {

		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> METHOD_WITH_PARAMETERS.getParams().addAll(DUMMY_PARAMS));

		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> copyJavaMethod(METHOD_WITH_PARAMETERS).getParams().addAll(DUMMY_PARAMS));
	}

	@TestOnly
	private void testMethodWithParameters(int param1, String param2) {
	}

	@TestOnly
	private void testMethodWithoutParametersOrReturnType() {
	}

	@TestOnly
	@SuppressWarnings("SameReturnValue")
	private Integer testMethodWithReturnType() {
		return 0;
	}
}
