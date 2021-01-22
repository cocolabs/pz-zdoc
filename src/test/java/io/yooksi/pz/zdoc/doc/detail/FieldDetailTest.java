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

import static io.yooksi.pz.zdoc.doc.detail.FieldDetail.Signature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;
import zombie.core.Color;

@SuppressWarnings("SpellCheckingInspection")
class FieldDetailTest extends FieldDetailTestFixture {
	FieldDetailTest() throws DetailParsingException {
	}

	@Test
	void shouldMatchAccessModifierInFieldSignature() throws DetailParsingException {

		Map<AccessModifierKey, String> fieldsWithAccessKeyword = ImmutableMap.of(
				AccessModifierKey.PUBLIC, "public int myField",
				AccessModifierKey.PRIVATE, "private boolean myField",
				AccessModifierKey.PROTECTED, "protected char myField",
				AccessModifierKey.DEFAULT, "java.lang.String myField"
		);
		for (Map.Entry<AccessModifierKey, String> entry : fieldsWithAccessKeyword.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertEquals(entry.getKey(), signature.modifier.getAccess());
		}
	}

	@Test
	void shouldMatchNonAccessModifiersInFieldSignature() throws DetailParsingException {

		Map<ModifierKey, String> fieldsWithNonAccessModifier = ImmutableMap.of(
				ModifierKey.STATIC, "static int myField",
				ModifierKey.FINAL, "final char myField",
				ModifierKey.ABSTRACT, "abstract java.lang.Object myField"
		);
		for (Map.Entry<ModifierKey, String> entry : fieldsWithNonAccessModifier.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertTrue(signature.modifier.matchesModifiers(entry.getKey()));
		}
	}

	@Test
	void shouldMatchMixedModifiersInFieldSignature() throws DetailParsingException {

		Map<MemberModifier, String> fieldsWithMixedModifiers = ImmutableMap.of(
				new MemberModifier(AccessModifierKey.PUBLIC, ModifierKey.STATIC),
				"public static float myField",

				new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL),
				"private static final java.lang.Object myField",

				new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.ABSTRACT),
				"abstract int myField"
		);
		for (Map.Entry<MemberModifier, String> entry : fieldsWithMixedModifiers.entrySet())
		{
			Signature signature = new Signature(entry.getValue());
			Assertions.assertEquals(entry.getKey(), signature.modifier);
		}
	}

	@Test
	void shouldMatchObjectTypeInFieldSignature() {

		Map<String, String> fieldsWithObjectType = ImmutableMap.of(
				"int", "int myField",
				"boolean[]", "boolean[] myField",
				"java.lang.String", "java.lang.String myField",
				"java.lang.Object[]", "final java.lang.Object[] myField"
		);
		for (Map.Entry<String, String> entry : fieldsWithObjectType.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), TypeSupplier.class);
		}
	}

	@Test
	void shouldMatchParameterizedObjectTypeInFieldSignature() {

		Map<String, String> fieldsWithParameterizedObjectType = ImmutableMap.of(
				"java.util.ArrayList<java.lang.Class<?>,java.lang.Class<?>>",
				"java.util.ArrayList<java.lang.Class<?>,java.lang.Class<?>> myField",

				"java.util.ArrayList<java.lang.Class<?>>",
				"java.util.ArrayList<java.lang.Class<?>> myField",

				"java.util.ArrayList<java.lang.String>",
				"final java.util.ArrayList<java.lang.String> myField",

				"java.util.ArrayList<T>",
				"java.util.ArrayList<T> myField"
		);
		for (Map.Entry<String, String> entry : fieldsWithParameterizedObjectType.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), TypeSupplier.class);
		}
	}

	@Test
	void shouldMatchPrimitiveReturnTypeInFieldSignature() {

		for (String type : PRIMITIVE_TYPES) {
			assertMatchInSignature(type + " myField", type, TypeSupplier.class);
		}
	}

	@Test
	void shouldMatchNameInFieldSignature() {

		Map<String, String> fieldsWithNames = ImmutableMap.of(
				"myField", "int myField",
				"my_field", "char my_field",
				"my$field", "java.lang.Integer my$field",
				"1myF13ld", "java.lang.Object[] 1myF13ld",
				"field", "my.Test<my_Class<?>> field"
		);
		for (Map.Entry<String, String> entry : fieldsWithNames.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), NameSupplier.class);
		}
	}

	@Test
	void shouldMatchCommentInMethodSignature() {

		Map<String, String> methodsWithComment = ImmutableMap.of(
				"// comment", "void myField // comment",
				"onewordcomment", "int myField onewordcomment",
				"multi word comment", "java.lang.ArrayList<Class<?>> myField multi word comment",
				"!@# $%^&*_ {()comment", "char[] myField !@# $%^&*_ {()comment"
		);
		for (Map.Entry<String, String> entry : methodsWithComment.entrySet()) {
			assertMatchInSignature(entry.getValue(), entry.getKey(), CommentSupplier.class);
		}
	}

	@Test
	void shouldParseValidModifierKeyFromFieldDetail() {

		List<JavaField> entries = detail.getEntries();
		ModifierKey[][] expectedFieldModifierKeys = new ModifierKey[][]{
				new ModifierKey[]{
						ModifierKey.UNDECLARED,        // a
				},
				new ModifierKey[]{
						ModifierKey.FINAL              // b
				},
				new ModifierKey[]{
						ModifierKey.STATIC,            // black
						ModifierKey.FINAL
				},
				new ModifierKey[]{
						ModifierKey.STATIC,            // blue
				},
				new ModifierKey[]{
						ModifierKey.UNDECLARED         // cyan
				},
		};
		Assertions.assertEquals(expectedFieldModifierKeys.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			ModifierKey[] keys = expectedFieldModifierKeys[i];
			Assertions.assertTrue(entries.get(i).getModifier().matchesModifiers(keys));
		}
	}

	@Test
	void shouldParseValidAccessModifierFromFieldDetail() {

		List<JavaField> entries = detail.getEntries();
		AccessModifierKey[] expectedModifiers = new AccessModifierKey[]{
				AccessModifierKey.PUBLIC,                   // begin
				AccessModifierKey.PRIVATE,                  // doesInstantly
				AccessModifierKey.PROTECTED,                // init
				AccessModifierKey.DEFAULT,                  // isFinished
				AccessModifierKey.PUBLIC,                   // update
		};
		Assertions.assertEquals(expectedModifiers.length, entries.size());
		for (int i = 0; i < entries.size(); i++) {
			Assertions.assertTrue(entries.get(i).getModifier().hasAccess(expectedModifiers[i]));
		}
	}

	@Test
	void shouldParseValidObjectTypesFromFieldDetail() {

		List<JavaField> entries = detail.getEntries();
		JavaClass[] expectedTypes = new JavaClass[]{
				new JavaClass(float.class),                         // a
				new JavaClass(Integer.class),                       // b
				new JavaClass(Color.class),                         // black
				new JavaClass(Color[].class),                       // blue
				new JavaClass(ArrayList.class,                        // cyan
						new JavaClass(Color.class))
		};
		Assertions.assertEquals(expectedTypes.length, entries.size());
		for (int i = 0; i < expectedTypes.length; i++) {
			Assertions.assertEquals(expectedTypes[i], entries.get(i).getType());
		}
	}

	@Test
	void shouldParseValidNamesFromFieldDetail() {

		List<JavaField> entries = detail.getEntries();
		String[] expectedFieldNames = new String[]{
				"a", "b", "black", "blue", "cyan"
		};
		Assertions.assertEquals(expectedFieldNames.length, entries.size());
		for (int i = 0; i < entries.size(); i++)
		{
			JavaField entry = entries.get(i);
			Assertions.assertEquals(expectedFieldNames[i], entry.getName());
		}
	}

	@Test
	void shouldGetCorrectFieldDetailEntriesByName() {

		List<JavaField> expectedJavaFieldEntries = ImmutableList.of(
				new JavaField(float.class, "a", new MemberModifier(
						AccessModifierKey.PUBLIC, ModifierKey.UNDECLARED
				)),
				new JavaField(Integer.class, "b", new MemberModifier(
						AccessModifierKey.PRIVATE, ModifierKey.FINAL
				)),
				new JavaField(Color.class, "black", new MemberModifier(
						AccessModifierKey.PROTECTED, ModifierKey.STATIC, ModifierKey.FINAL
				)),
				new JavaField(Color[].class, "blue", new MemberModifier(
						AccessModifierKey.DEFAULT, ModifierKey.STATIC
				)),
				new JavaField(new JavaClass(ArrayList.class, new JavaClass(Color.class)),
						"cyan", new MemberModifier(AccessModifierKey.PUBLIC)
				)
		);
		Assertions.assertEquals(expectedJavaFieldEntries.size(), detail.getEntries().size());
		for (JavaField field : expectedJavaFieldEntries) {
			Assertions.assertEquals(field, detail.getEntry(field.getName()));
		}
	}
}
