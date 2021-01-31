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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.yooksi.pz.zdoc.JavaClassUtils;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaParameter;

class MethodSignatureParserTest {

	@Test
	void shouldThrowExceptionWhenParsingUnknownClasses() {

		Set<String> unknownMethodSignatures = Sets.newHashSet(
				"io.yooksi.unknownClass param",
				"io.yooksi.unknownClass<java.lang.Object> param",
				"io.yooksi.unknownClass<java.lang.Object, java.lang.String> param",
				"java.lang.Object param0, io.yooksi.unknownClass param1",
				"java.lang.Object param0, io.yooksi.unknownClass<java.lang.Object> param",
				"java.lang.Object param0, io.yooksi.unknownClass<java.lang.Object, java.lang.String> param"
		);
		for (String signature : unknownMethodSignatures) {
			Assertions.assertThrows(SignatureParsingException.class,
					() -> new MethodSignatureParser(signature).parse());
		}
	}

	@Test
	void shouldThrowExceptionWhenParsingMalformedSignature() {

		Set<String> malformedSignatures = Sets.newHashSet(
				"someParameterName",        // missing parameter type
				"java.lang.Object",                   // missing parameter name
				" ,"                                  // missing parameter type and name
		);
		for (String signature : malformedSignatures) {
			Assertions.assertThrows(SignatureParsingException.class,
					() -> new MethodSignatureParser(signature).parse());
		}
	}

	@Test
	void shouldParseJavaParametersFromSignature() throws SignatureParsingException {

		List<String> parameterSignatures = ImmutableList.of(
				"java.lang.String param",
				"java.lang.String[] params",
				"java.lang.Object param0, java.lang.String param1",
				"java.lang.Object param0, java.lang.String param1, java.lang.Integer param2",
				"java.lang.Object[] param0, java.lang.String[] param1",
				"int[] params", "int param0, boolean param1",
				"int[] params0, boolean[] params1, char param2"
		);
		List<List<JavaParameter>> expectedParameters = ImmutableList.of(
				Collections.singletonList(
						new JavaParameter(new JavaClass(String.class), "param")
				),
				Collections.singletonList(
						new JavaParameter(new JavaClass(String[].class), "params")
				),
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object.class), "param0"),
						new JavaParameter(new JavaClass(String.class), "param1")
				),
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object.class), "param0"),
						new JavaParameter(new JavaClass(String.class), "param1"),
						new JavaParameter(new JavaClass(Integer.class), "param2")
				),
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object[].class), "param0"),
						new JavaParameter(new JavaClass(String[].class), "param1")
				),
				Collections.singletonList(
						new JavaParameter(new JavaClass(int[].class), "params")
				),
				ImmutableList.of(
						new JavaParameter(new JavaClass(int.class), "param0"),
						new JavaParameter(new JavaClass(boolean.class), "param1")
				),
				ImmutableList.of(
						new JavaParameter(new JavaClass(int[].class), "params0"),
						new JavaParameter(new JavaClass(boolean[].class), "params1"),
						new JavaParameter(new JavaClass(char.class), "param2")
				)
		);
		for (int i = 0; i < parameterSignatures.size(); i++)
		{
			String signature = parameterSignatures.get(i);
			List<JavaParameter> actual = new MethodSignatureParser(signature).parse();
			Assertions.assertEquals(expectedParameters.get(i), actual);
		}
	}

	@Test
	void shouldParseJavaParametersWithTypeParametersFromSignature() throws SignatureParsingException {

		List<String> parameterSignatures = ImmutableList.of(
				"java.util.ArrayList<java.lang.Object> param",
				"java.util.ArrayList<java.lang.Class<?>> param",
				"java.util.Map<java.lang.Object, java.lang.String>> param",
				"java.util.Map<java.lang.Integer, java.lang.Class<?>> param",
				"java.util.ArrayList<java.lang.Object> param0, " +
						"java.util.ArrayList<java.lang.Integer> param1",
				"java.util.ArrayList<java.lang.Class<?>> param0, " +
						"java.util.ArrayList<java.lang.Integer> param1",
				"java.util.Map<java.lang.Object, java.lang.String>> param0, " +
						"java.util.Map<java.lang.String, java.lang.Integer>> param1",
				"java.util.Map<java.lang.String, java.lang.Class<?>> param0, " +
						"java.util.Map<java.lang.Object, java.lang.Class<?>> param1"
		);
		List<List<JavaParameter>> expectedParameters = ImmutableList.of(
				// java.util.ArrayList<java.lang.Object> param
				Collections.singletonList(
						new JavaParameter(JavaClassUtils.getList(Object.class), "param")
				),
				// java.util.ArrayList<java.lang.Class<?>> param
				Collections.singletonList(
						new JavaParameter(JavaClassUtils.getList(JavaClassUtils.CLASS), "param")
				),
				// java.util.Map<java.lang.Object, java.lang.String>> param
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(Object.class, String.class), "param")
				),
				// java.util.Map<java.lang.Integer, java.lang.Class<?>> param
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(
								Integer.class, JavaClassUtils.CLASS), "param")
				),
				// java.util.ArrayList<java.lang.Object> param0,
				// 		java.util.ArrayList<java.lang.Integer> param1
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getList(Object.class), "param0"),
						new JavaParameter(JavaClassUtils.getList(Integer.class), "param1")
				),
				// java.util.ArrayList<java.lang.Class<?>> param0,
				// 		java.util.ArrayList<java.lang.Integer> param1
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getList(JavaClassUtils.CLASS), "param0"),
						new JavaParameter(JavaClassUtils.getList(Integer.class), "param1")
				),
				// java.util.Map<java.lang.Object, java.lang.String>> param0,
				// 		java.util.Map<java.lang.String, java.lang.Integer>> param
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(Object.class, String.class), "param0"),
						new JavaParameter(JavaClassUtils.getMap(String.class, Integer.class), "param1")
				),
				// java.util.Map<java.lang.String, java.lang.Class<?>> param0,
				// 		java.util.Map<java.lang.Object, java.lang.Class<?>> param1
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(
								String.class, JavaClassUtils.CLASS), "param0"
						),
						new JavaParameter(JavaClassUtils.getMap(
								Object.class, JavaClassUtils.CLASS), "param1")
				)
		);
		for (int i = 0; i < parameterSignatures.size(); i++)
		{
			String signature = parameterSignatures.get(i);
			List<JavaParameter> actual = new MethodSignatureParser(signature).parse();
			Assertions.assertEquals(expectedParameters.get(i), actual);
		}
	}

	@Test
	void shouldParseJavaParametersWithVariadicArgumentsFromSignature() throws SignatureParsingException {

		List<String> parameterSignatures = ImmutableList.of(
				"java.lang.Object param, java.lang.String... varargs0",
				"java.lang.Object param, int... varargs1",
				"java.lang.Object param0, java.lang.String param1, boolean... varargs2",
				"java.util.ArrayList<java.lang.Class<?>> param0, char param1, java.lang.String... varargs3",
				"boolean[] param0, java.lang.String[] param1, " +
						"java.util.ArrayList<java.lang.Class<?>>... varargs4"
		);
		List<List<JavaParameter>> expectedParameters = ImmutableList.of(
				// java.lang.Object param, java.lang.String... varargs
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object.class), "param"),
						new JavaParameter(new JavaClass(String.class), "varargs0")
				),
				// java.lang.Object param, int... varargs
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object.class), "param"),
						new JavaParameter(new JavaClass(int.class), "varargs1")
				),
				// java.lang.Object param0, java.lang.String param1, boolean... varargs
				ImmutableList.of(
						new JavaParameter(new JavaClass(Object.class), "param0"),
						new JavaParameter(new JavaClass(String.class), "param1"),
						new JavaParameter(new JavaClass(boolean.class), "varargs2")
				),
				// java.util.ArrayList<java.lang.Class<?>> param0, char param1, java.lang.String... varargs
				ImmutableList.of(
						new JavaParameter(JavaClassUtils.getList(JavaClassUtils.CLASS), "param0"),
						new JavaParameter(new JavaClass(char.class), "param1"),
						new JavaParameter(new JavaClass(String.class), "varargs3")
				),
				// boolean[] param0, java.lang.String[] param1, java.util.ArrayList<java.lang.Class<?>>...
				// varargs
				ImmutableList.of(
						new JavaParameter(new JavaClass(boolean[].class), "param0"),
						new JavaParameter(new JavaClass(String[].class), "param1"),
						new JavaParameter(JavaClassUtils.getList(JavaClassUtils.CLASS), "varargs4")
				)
		);
		for (int i = 0; i < parameterSignatures.size(); i++)
		{
			String signature = parameterSignatures.get(i);
			MethodSignatureParser parser = new MethodSignatureParser(signature);
			Assertions.assertEquals(expectedParameters.get(i), parser.parse());
			Assertions.assertTrue(parser.isVarArg());
		}
	}
}
