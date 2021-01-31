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

import static io.yooksi.pz.zdoc.doc.detail.MethodDetail.Signature;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.yooksi.pz.zdoc.JavaClassUtils;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;
import zombie.characters.IsoPlayer;
import zombie.core.Color;

@SuppressWarnings("SpellCheckingInspection")
class MethodDetailTest extends MethodDetailTestFixture {
	MethodDetailTest() throws DetailParsingException {
	}

	@Test
	void shouldMatchAccessModifierInMethodSignature() throws DetailParsingException {

		Map<AccessModifierKey, String> methodsWithAccessKeyword = ImmutableMap.of(
				AccessModifierKey.PUBLIC, "public void myMethod()",
				AccessModifierKey.PRIVATE, "private void myMethod()",
				AccessModifierKey.PROTECTED, "protected void myMethod()",
				AccessModifierKey.DEFAULT, "void myMethod()"
		);
		for (Map.Entry<AccessModifierKey, String> entry : methodsWithAccessKeyword.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertEquals(entry.getKey(), signature.modifier.getAccess());
		}
	}

	@Test
	void shouldMatchNonAccessModifiersInMethodSignature() throws DetailParsingException {

		Map<ModifierKey, String> methodsWithNonAccessModifier = ImmutableMap.of(
				ModifierKey.STATIC, "static void myMethod()",
				ModifierKey.FINAL, "final void myMethod()",
				ModifierKey.ABSTRACT, "abstract void myMethod()"
		);
		for (Map.Entry<ModifierKey, String> entry : methodsWithNonAccessModifier.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertTrue(signature.modifier.matchesModifiers(entry.getKey()));
		}
	}

	@Test
	void shouldMatchMixedModifiersInMethodSignature() throws DetailParsingException {

		Map<MemberModifier, String> methodsWithMixedModifiers = ImmutableMap.of(
				new MemberModifier(AccessModifierKey.PUBLIC, ModifierKey.STATIC),
				"public static void myMethod()",

				new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL),
				"private static final void myMethod()",

				new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.ABSTRACT),
				"abstract void myMethod()"
		);
		for (Map.Entry<MemberModifier, String> entry : methodsWithMixedModifiers.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertEquals(entry.getKey(), signature.modifier);
		}
	}

	@Test
	void shouldMatchReturnTypeInMethodSignature() {

		Map<String, String> methodsWithReturnType = ImmutableMap.of(
				"void", "void myMethod()",
				"int[]", "int[] myMethod(boolean param)",
				"java.lang.String", "java.lang.String myMethod()",
				"java.lang.Object", "java.lang.Object myMethod()",
				"java.lang.Object[]", "java.lang.Object[] myMethod()"
		);
		for (Map.Entry<String, String> entry : methodsWithReturnType.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), ReturnTypeSupplier.class);
		}
	}

	@Test
	void shouldMatchParameterizedReturnTypeInMethodSignature() {

		Map<String, String> methodsWithParameterizedReturnType = ImmutableMap.of(
				"java.util.ArrayList<java.lang.Class<?>>",
				"java.util.ArrayList<java.lang.Class<?>> myMethod()",

				"java.util.ArrayList<java.lang.String>",
				"java.util.ArrayList<java.lang.String> myMethod()",

				"java.util.ArrayList<T>",
				"java.util.ArrayList<T> myMethod()"
		);
		for (Map.Entry<String, String> entry : methodsWithParameterizedReturnType.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), ReturnTypeSupplier.class);
		}
	}

	@Test
	void shouldMatchPrimitiveReturnTypeInMethodSignature() {

		for (String type : PRIMITIVE_TYPES) {
			assertMatchInSignature(type + " myMethod()", type, ReturnTypeSupplier.class);
		}
	}

	@Test
	void shouldMatchNameInMethodSignature() {

		Map<String, String> methodsWithNames = ImmutableMap.of(
				"myMethod", "void myMethod(java.lang.Object param)",
				"my_method", "void my_method(java.lang.Object param)",
				"my$method", "java.lang.Integer my$method(java.lang.Object param)",
				"1myM3thod", "void 1myM3thod(java.lang.Object param)",
				"method", "my.Test<my.Class<?>> method(java.lang.Object param)"
		);
		for (Map.Entry<String, String> entry : methodsWithNames.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), NameSupplier.class);
		}
	}

	@Test
	void shouldMatchParametersInMethodSignature() {

		Map<String, String> methodsWithParameters = ImmutableMap.<String, String>builder()
				.put("java.lang.Object param",
						"void myMethod(java.lang.Object param)")

				.put("java.lang.Object param1, java.lang.Object param2",
						"void myMethod(java.lang.Object param1, java.lang.Object param2)")

				.put("java.lang.Object param1, java.lang.Object param2, java.lang.String param3",
						"java.lang.Integer myMethod(java.lang.Object param1," +
								" java.lang.Object param2, java.lang.String param3)")

				.put("java.lang.Object param1, java.lang.Object param2, java.lang.Integer param3",
						"void myMethod(java.lang.Object param1, java.lang.Object param2, " +
								"java.lang.Integer param3)")

				.put("java.lang.Object param0, java.lang.Object param1",
						"void myMethod(java.lang.Object param0, java.lang.Object param1)")

				.put("java.lang.Object param1",
						"void myMethod(java.lang.Object param1)")

				.put("java.lang.Object param0",
						"java.util.ArrayList<java.lang.Class<?>> myMethod(java.lang.Object param0)").build();

		for (Map.Entry<String, String> entry : methodsWithParameters.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), ParamsSupplier.class);
		}
	}

	@Test
	void shouldMatchPrimitiveTypeParametersInMethodSignature() {

		Map<String, String> methodsWithPrimitiveTypeParameters = new HashMap<>();
		for (String type : PRIMITIVE_TYPES)
		{
			String param1 = type + " param";
			methodsWithPrimitiveTypeParameters.put(
					param1, String.format("void myMethod(%s)", param1)
			);
			String param2 = type + " param1, " + type + " param2";
			methodsWithPrimitiveTypeParameters.put(
					param2, String.format("void myMethod(%s)", param2)
			);
			String param3 = String.format("%s param1, %s param2, %s param3", type, type, type);
			methodsWithPrimitiveTypeParameters.put(
					param3, String.format("java.lang.Integer myMethod(%s)", param3)
			);
			methodsWithPrimitiveTypeParameters.put(
					param3, String.format("void myMethod(%s)", param3)
			);
			methodsWithPrimitiveTypeParameters.put(
					param2, String.format("void myMethod(%s)", param2)
			);
			methodsWithPrimitiveTypeParameters.put(
					param1, String.format("void myMethod(%s)", param1)
			);
			for (Map.Entry<String, String> entry : methodsWithPrimitiveTypeParameters.entrySet()) {
				assertMatchInSignature(entry.getValue(), entry.getKey(), ParamsSupplier.class);
			}
		}
	}

	@Test
	void shouldMatchVarArgParametersInMethodSignature() {

		Map<String, String> methodsWithVarArgParameters = ImmutableMap.<String, String>builder()
				.put("java.lang.Object...params0",
						"void myMethod(java.lang.Object...params0)")

				.put("java.lang.Object param1, java.lang.Object...params",
						"void myMethod(java.lang.Object param1, java.lang.Object...params)")

				.put("java.lang.Object param1, java.lang.Object param2, java.lang.String...params",
						"java.lang.Integer myMethod(java.lang.Object param1," +
								" java.lang.Object param2, java.lang.String...params)")

				.put("java.lang.Object param1, java.lang.Object param2, java.lang.Object...params",
						"void myMethod(java.lang.Object param1, java.lang.Object param2, " +
								"java.lang.Object...params)")

				.put("java.lang.Object param0, java.lang.Object...params",
						"void myMethod(java.lang.Object param0, java.lang.Object...params)")

				.put("java.lang.Object...params1",
						"void myMethod(java.lang.Object...params1)")

				.put("java.lang.Object...params2",
						"java.util.ArrayList<java.lang.Class<?>> myMethod(java.lang.Object...params2)").build();

		for (Map.Entry<String, String> entry : methodsWithVarArgParameters.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), ParamsSupplier.class);
		}
	}

	@Test
	void shouldMatchCommentInMethodSignature() {

		Map<String, String> methodsWithComment = ImmutableMap.<String, String>builder()
				.put("// comment", "void myMethod() // comment")
				.put("onewordcomment", "void myMethod() onewordcomment")
				.put("multi word comment", "void myMethod()multi word comment")
				.put("samplecomment", "void myMethod(int param) samplecomment")
				.put("sample comment", "void myMethod(int param)sample comment")
				.put("!@# $%^&*_ {()comment", "void myMethod()!@# $%^&*_ {()comment").build();

		for (Map.Entry<String, String> entry : methodsWithComment.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), CommentSupplier.class);
		}
	}

	@Test
	void shouldIncludeAnnotationInMethodSignatureComment() throws SignatureParsingException {

		Map<String, String> annotatedMethods = ImmutableMap.<String, String>builder()
				.put("%s @Deprecated", "@Deprecated\nvoid myMethod()")
				.put("comment\n%s @Deprecated", "@Deprecated\rvoid myMethod() comment")
				.put("%s @Immutable", "@Immutable\r\njava.lang.Object myMethod()")
				.put("\\\\ comment\n%s @Immutable", "@Immutable int myMethod()\\\\ comment")
				.put("\\\\ some comment\n%s @TestOnly", "@TestOnly void myMethod() \\\\ some comment").build();

		for (Map.Entry<String, String> entry : annotatedMethods.entrySet())
		{
			String expected = String.format(entry.getKey(), "This method is annotated as");
			String actual = new Signature(entry.getValue()).comment;
			Assertions.assertEquals(actual, expected);
		}
	}

	@Test
	void shouldParseValidModifierKeyFromMethodDetail() {

		List<JavaMethod> entries = detail.getEntries();
		ModifierKey[][] expectedMethodModifierKeys = new ModifierKey[][]{
				new ModifierKey[]{
						ModifierKey.UNDECLARED,          // begin
				},
				new ModifierKey[]{
						ModifierKey.STATIC               // doesInstantly
				},
				new ModifierKey[]{
						ModifierKey.STATIC,              // init
						ModifierKey.FINAL
				},
				new ModifierKey[]{
						ModifierKey.UNDECLARED           // isFinished
				},
				new ModifierKey[]{
						ModifierKey.STATIC               // update
				},
				new ModifierKey[]{
						ModifierKey.STATIC               // getActivatedMods
				},
				new ModifierKey[]{
						ModifierKey.UNDECLARED           // getColor
				},
				new ModifierKey[]{
						ModifierKey.UNDECLARED           // doTask
				}
		};
		Assertions.assertEquals(expectedMethodModifierKeys.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			ModifierKey[] keys = expectedMethodModifierKeys[i];
			Assertions.assertTrue(entries.get(i).getModifier().matchesModifiers(keys));
		}
	}

	@Test
	void shouldParseValidAccessModifierFromMethodDetail() {

		List<JavaMethod> entries = detail.getEntries();
		AccessModifierKey[] expectedMethodAccessModifiers = new AccessModifierKey[]{
				AccessModifierKey.PUBLIC,                   // begin
				AccessModifierKey.PROTECTED,                // doesInstantly
				AccessModifierKey.PRIVATE,                  // init
				AccessModifierKey.DEFAULT,                  // isFinished
				AccessModifierKey.DEFAULT,                  // update
				AccessModifierKey.DEFAULT,                  // getActivatedMods
				AccessModifierKey.PUBLIC,                   // getColor
				AccessModifierKey.PUBLIC                    // doTask
		};
		Assertions.assertEquals(expectedMethodAccessModifiers.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			JavaMethod entry = entries.get(i);
			Assertions.assertTrue(entry.getModifier().hasAccess(expectedMethodAccessModifiers[i]));
		}
	}

	@Test
	void shouldParseValidMethodReturnTypeFromMethodDetail() {

		List<JavaMethod> entries = detail.getEntries();
		JavaClass[] expectedMethodReturnTypes = new JavaClass[]{
				new JavaClass(int.class),                           // begin
				new JavaClass(boolean.class),                       // doesInstantly
				new JavaClass(String.class),                        // init
				new JavaClass(Object[].class),                      // isFinished
				new JavaClass(void.class),                          // update
				new JavaClass(ArrayList.class,                      // getActivatedMods
						new JavaClass(String.class)),
				new JavaClass(Color[].class),                       // getColor
				new JavaClass(void.class),                        // doTask
		};
		Assertions.assertEquals(expectedMethodReturnTypes.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			JavaMethod entry = entries.get(i);
			Assertions.assertEquals(expectedMethodReturnTypes[i], entry.getReturnType());
		}
	}

	@Test
	void shouldParseValidMethodNamesFromMethodDetail() {

		List<JavaMethod> entries = detail.getEntries();
		String[] expectedMethodNames = new String[]{
				"begin", "DoesInstantly", "init", "IsFinished",
				"update", "getActivatedMods", "getColor", "doTask"
		};
		Assertions.assertEquals(expectedMethodNames.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			JavaMethod entry = entries.get(i);
			Assertions.assertEquals(expectedMethodNames[i], entry.getName());
		}
	}

	@Test
	void shouldParseValidMethodParamsFromMethodDetail() {

		List<JavaMethod> entries = detail.getEntries();
		JavaParameter[][] expectedMethodParams = new JavaParameter[][]{
				new JavaParameter[]{                                        // begin
						new JavaParameter(Object.class, "param")
				},
				new JavaParameter[]{                                        // doesInstantly
						new JavaParameter(int.class, "number")
				},
				new JavaParameter[]{                                        // init
						new JavaParameter(String.class, "object"),
						new JavaParameter(String[].class, "params")
				},
				new JavaParameter[]{},                                      // isFinished
				new JavaParameter[]{                                        // update
						new JavaParameter(new JavaClass(ArrayList.class,
								new JavaClass(String.class)), "params"),
				},
				new JavaParameter[]{},                                      // getActivatedMods
				new JavaParameter[]{                                        // getColor
						new JavaParameter(IsoPlayer.class, "player")
				},
				new JavaParameter[]{                                        // getColor
						new JavaParameter(new JavaClass(Map.class, ImmutableList.of(
								new JavaClass(Map.class, ImmutableList.of(
										new JavaClass(Class.class, 1),
										new JavaClass(Object.class)
								)), new JavaClass(Object.class))), "map"
						),
						new JavaParameter(new JavaClass(Object.class), "obj")
				}
		};
		Assertions.assertEquals(expectedMethodParams.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			JavaMethod entry = entries.get(i);
			Assertions.assertEquals(Arrays.asList(expectedMethodParams[i]), entry.getParams());
		}
	}

	@Test
	void shouldGetCorrectMethodDetailEntriesByName() {

		List<JavaMethod> expectedJavaMethodEntries = ImmutableList.of(
				new JavaMethod("begin", int.class, ImmutableList.of(
						new JavaParameter(Object.class, "param")
				), new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("DoesInstantly", boolean.class, ImmutableList.of(
						new JavaParameter(int.class, "number")
				), new MemberModifier(AccessModifierKey.PROTECTED, ModifierKey.STATIC)
				),
				new JavaMethod("init", String.class, ImmutableList.of(
						new JavaParameter(String.class, "object"),
						new JavaParameter(String[].class, "params")
				), new MemberModifier(
						AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL
				)),
				new JavaMethod("IsFinished", Object[].class, MemberModifier.UNDECLARED
				),
				new JavaMethod("update", void.class, ImmutableList.of(
						new JavaParameter(
								new JavaClass(ArrayList.class, new JavaClass(String.class)),
								"params"
						)
				), new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getActivatedMods", new JavaClass(
						ArrayList.class, new JavaClass(String.class)
				), ImmutableList.of(),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getColor", Color[].class, ImmutableList.of(
						new JavaParameter(new JavaClass(IsoPlayer.class), "player")
				), new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("doTask", void.class, ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(JavaClassUtils.getMap(
								JavaClassUtils.CLASS, Object.class), Object.class), "map"
						),
						new JavaParameter(Object.class, "obj")
				), new MemberModifier(AccessModifierKey.PUBLIC))
		);
		Assertions.assertEquals(expectedJavaMethodEntries.size(), detail.getEntries().size());
		for (JavaMethod field : expectedJavaMethodEntries) {
			Assertions.assertTrue(detail.getEntries(field.getName()).contains(field));
		}
	}
}
