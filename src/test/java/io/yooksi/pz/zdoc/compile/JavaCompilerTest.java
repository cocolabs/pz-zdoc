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
package io.yooksi.pz.zdoc.compile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.yooksi.pz.zdoc.JavaClassUtils;
import io.yooksi.pz.zdoc.doc.DocTest;
import io.yooksi.pz.zdoc.doc.detail.DetailParsingException;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;
import zombie.characters.IsoPlayer;
import zombie.core.Color;

@Tag("compile")
class JavaCompilerTest extends DocTest {

	@Test
	void shouldCompileDeclaredJavaFieldsFromClassWithNullDocument() throws DetailParsingException {

		List<JavaField> expectedJavaFields = Arrays.asList(
				new JavaField(float.class, "a", new MemberModifier(
						AccessModifierKey.PUBLIC
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
				new JavaField(new JavaClass(ArrayList.class, 1),
						"cyan", new MemberModifier(AccessModifierKey.PUBLIC))
		);
		List<JavaField> compiledFields = JavaCompiler.compileJavaFields(CompileTest.class, null);
		Assertions.assertEquals(expectedJavaFields, compiledFields);
	}

	@Test
	void shouldCompileDeclaredJavaFieldsFromClassWithDocument() throws DetailParsingException {

		List<JavaField> expectedJavaFields = Arrays.asList(
				new JavaField(float.class, "a", new MemberModifier(
						AccessModifierKey.PUBLIC
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
						"cyan", new MemberModifier(AccessModifierKey.PUBLIC))
		);
		List<JavaField> compiledFields = JavaCompiler.compileJavaFields(CompileTest.class, DOCUMENT);
		Assertions.assertEquals(expectedJavaFields, compiledFields);
	}

	@Test
	void shouldCompileDeclaredJavaMethodsFromClassWithNullDocument() throws DetailParsingException {

		Set<JavaMethod> expectedJavaMethods = Sets.newHashSet(
				new JavaMethod("begin", int.class,
						new JavaParameter(Object.class, "arg0"),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("DoesInstantly", boolean.class,
						new JavaParameter(int.class, "arg0"),
						new MemberModifier(AccessModifierKey.PROTECTED, ModifierKey.STATIC)
				),
				new JavaMethod("init", String.class,
						Arrays.asList(
								new JavaParameter(String.class, "arg0"),
								new JavaParameter(String[].class, "arg1")
						),
						new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL)
				),
				new JavaMethod("IsFinished", Object[].class, MemberModifier.UNDECLARED
				),
				new JavaMethod("update", void.class,
						new JavaParameter(new JavaClass(ArrayList.class, 1), "arg0"),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getActivatedMods", new JavaClass(ArrayList.class, 1),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getColor", Color[].class,
						new JavaParameter(IsoPlayer.class, "arg0"),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("doTask", void.class, ImmutableList.of(
						new JavaParameter(new JavaClass(Map.class, 2), "arg0"),
						new JavaParameter(Object.class, "arg1")
				), new MemberModifier(AccessModifierKey.PUBLIC))
		);
		Set<JavaMethod> compiledMethods = JavaCompiler.compileJavaMethods(CompileTest.class, null);
		Assertions.assertEquals(expectedJavaMethods, compiledMethods);
	}

	@Test
	void shouldCompileDeclaredJavaMethodsFromClassWithDocument() throws DetailParsingException {

		Set<JavaMethod> expectedJavaMethods = Sets.newHashSet(
				new JavaMethod("begin", int.class,
						new JavaParameter(Object.class, "param"),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("DoesInstantly", boolean.class,
						new JavaParameter(int.class, "number"),
						new MemberModifier(AccessModifierKey.PROTECTED, ModifierKey.STATIC)
				),
				new JavaMethod("init", String.class,
						Arrays.asList(
								new JavaParameter(String.class, "object"),
								new JavaParameter(String[].class, "params")
						),
						new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL)
				),
				new JavaMethod("IsFinished", Object[].class, MemberModifier.UNDECLARED
				),
				new JavaMethod("update", void.class,
						new JavaParameter(new JavaClass(ArrayList.class, new JavaClass(String.class)),
								"params"),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getActivatedMods",
						new JavaClass(ArrayList.class, new JavaClass(String.class)),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getColor", Color[].class,
						new JavaParameter(IsoPlayer.class, "player"),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("doTask", void.class, ImmutableList.of(
						new JavaParameter(JavaClassUtils.getMap(
								JavaClassUtils.getMap(JavaClassUtils.CLASS, Object.class), Object.class
						), "map"),
						new JavaParameter(Object.class, "obj")
				), new MemberModifier(AccessModifierKey.PUBLIC))
		);
		Set<JavaMethod> compiledMethods = JavaCompiler.compileJavaMethods(CompileTest.class, DOCUMENT);
		Assertions.assertEquals(expectedJavaMethods, compiledMethods);
	}

	@Test
	void shouldNotCompileSyntheticConstructsFromClass() throws DetailParsingException {

		Class<?> nestedClass = CompileSyntheticTest.NestedClass.class;
		Method[] nestedMethods = nestedClass.getDeclaredMethods();
		// when building on Unix CI we find 3 declared methods instead of 2
		Assertions.assertTrue(nestedMethods.length > 1 && nestedMethods.length < 4);

		for (Method declaredMethod : nestedMethods) {
			Assertions.assertTrue(declaredMethod.isSynthetic());
		}
		Set<JavaMethod> compiledMethods =  JavaCompiler.compileJavaMethods(nestedClass, null);
		Assertions.assertEquals(0, compiledMethods.size());

		Field[] nestedFields = nestedClass.getDeclaredFields();
		// when building on Unix CI we find 3 declared fields instead of 2
		Assertions.assertTrue(nestedFields.length > 1 && nestedFields.length < 4);

		Assertions.assertFalse(nestedFields[0].isSynthetic());
		Assertions.assertTrue(nestedFields[1].isSynthetic());

		List<JavaField> compiledFields =  JavaCompiler.compileJavaFields(nestedClass, null);
		Assertions.assertEquals(1, compiledFields.size());
		Assertions.assertEquals("nestedField", compiledFields.get(0).getName());
	}

	@SuppressWarnings({ "unused", "SameReturnValue" })
	private static final class CompileTest {

		//@formatter:off
		public float a;
		private final Integer b = null;
		protected static final Color black = null;
		static Color[] blue;
		public ArrayList<Color> cyan;
		//@formatter:on

		protected static boolean DoesInstantly(int number) {
			return false;
		}

		@SuppressWarnings({ "FinalPrivateMethod", "FinalStaticMethod" })
		private static final String init(String object, String[] params) {
			return "";
		}

		@TestOnly
		static void update(ArrayList<String> params) {
		}

		static ArrayList<String> getActivatedMods() {
			return new ArrayList<>();
		}

		public int begin(Object param) {
			return 0;
		}

		Object[] IsFinished() {
			return new Object[0];
		}

		public Color[] getColor(IsoPlayer player) {
			return new Color[0];
		}

		public void doTask(Map<Map<Class<?>, Object>, Object> map, Object obj) {
		}
	}

	private static class CompileSyntheticTest {

		@SuppressWarnings({ "WeakerAccess", "InnerClassMayBeStatic" })
		class NestedClass {
			private String nestedField;
		}

		public String getNestedField() {
			return new NestedClass().nestedField;
		}

		public void setNestedField(String nestedField) {
			new NestedClass().nestedField = nestedField;
		}
	}
}
