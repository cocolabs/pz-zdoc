/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
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

import io.yooksi.pz.zdoc.IntegrationTest;
import io.yooksi.pz.zdoc.doc.DocTest;
import io.yooksi.pz.zdoc.doc.detail.DetailParsingException;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import zombie.characters.IsoPlayer;
import zombie.core.Color;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

@Tag("compile")
public class JavaCompilerTest extends DocTest implements IntegrationTest {

	private static final File EXPOSED_JAVA;

	static {
		try {
			ClassLoader cl = JavaCompilerTest.class.getClassLoader();
			EXPOSED_JAVA = new File(
					Objects.requireNonNull(cl.getResource("exposed.txt")).toURI()
			);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	private static abstract class CompileTest {

		public float a;
		private final Integer b = null;
		protected static final Color black = null;
		static Color[] blue;
		public ArrayList<Color> cyan;

		public int begin(Object param) {
			return 0;
		}
		protected static boolean DoesInstantly(int number) {
			return false;
		}
		@SuppressWarnings({ "FinalPrivateMethod", "FinalStaticMethod" })
		private static final String init(String object, String[] params) {
			return "";
		}
		Object[] IsFinished() {
			return new Object[0];
		}
		static void update(ArrayList<String> params) {
		}
		static ArrayList<String> getActivatedMods() {
			return new ArrayList<>();
		}
		public Color[] getColor(IsoPlayer player) {
			return new Color[0];
		}
	}

//	@Test
//	void shouldGetAllExposedJavaClasses() throws ReflectiveOperationException, IOException {
//
//		List<String> expectedExposedElements = FileUtils.readLines(EXPOSED_JAVA, Charset.defaultCharset());
//		HashSet<Class<?>> actualExposedElements = JavaCompiler.getExposedJava();
//
//		Assertions.assertEquals(expectedExposedElements.size(), actualExposedElements.size());
//		for (Class<?> actualExposedElement : actualExposedElements) {
//			Assertions.assertTrue(expectedExposedElements.contains(actualExposedElement.getName()));
//		}
//	}

	@Test
	void shouldCompileDeclaredJavaFieldsFromClassWithNullDocument() throws DetailParsingException {

		List<JavaField> expectedJavaFields = List.of(
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

		List<JavaField> expectedJavaFields = List.of(
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
				new JavaField(new JavaClass(ArrayList.class, List.of(new JavaClass(Color.class))),
						"cyan", new MemberModifier(AccessModifierKey.PUBLIC))
		);
		List<JavaField> compiledFields = JavaCompiler.compileJavaFields(CompileTest.class, document);
		Assertions.assertEquals(expectedJavaFields, compiledFields);
	}

	@Test
	void shouldCompileDeclaredJavaMethodsFromClassWithNullDocument() throws DetailParsingException {

		Set<JavaMethod> expectedJavaMethods = Set.of(
				new JavaMethod("begin", int.class,
						List.of(new JavaParameter(Object.class, "arg0")),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("DoesInstantly", boolean.class,
						List.of(new JavaParameter(int.class, "arg0")),
						new MemberModifier(AccessModifierKey.PROTECTED, ModifierKey.STATIC)
				),
				new JavaMethod("init", String.class,
						List.of(
							new JavaParameter(String.class, "arg0"),
							new JavaParameter(String[].class, "arg1")
						),
						new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL)
				),
				new JavaMethod("IsFinished", Object[].class, MemberModifier.UNDECLARED
				),
				new JavaMethod("update", void.class,
						List.of(new JavaParameter(
								new JavaClass(ArrayList.class, 1), "arg0"
						)),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getActivatedMods", new JavaClass(ArrayList.class, 1),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getColor", Color[].class,
						List.of(new JavaParameter(IsoPlayer.class, "arg0")),
						new MemberModifier(AccessModifierKey.PUBLIC)
				)
		);
		Set<JavaMethod> compiledMethods = JavaCompiler.compileJavaMethods(CompileTest.class, null);
		Assertions.assertEquals(expectedJavaMethods, compiledMethods);
	}

	@Test
	void shouldCompileDeclaredJavaMethodsFromClassWithDocument() throws DetailParsingException {

		Set<JavaMethod> expectedJavaMethods = Set.of(
				new JavaMethod("begin", int.class,
						List.of(new JavaParameter(Object.class, "param")),
						new MemberModifier(AccessModifierKey.PUBLIC)
				),
				new JavaMethod("DoesInstantly", boolean.class,
						List.of(new JavaParameter(int.class, "number")),
						new MemberModifier(AccessModifierKey.PROTECTED, ModifierKey.STATIC)
				),
				new JavaMethod("init", String.class,
						List.of(
								new JavaParameter(String.class, "object"),
								new JavaParameter(String[].class, "params")
						),
						new MemberModifier(AccessModifierKey.PRIVATE, ModifierKey.STATIC, ModifierKey.FINAL)
				),
				new JavaMethod("IsFinished", Object[].class, MemberModifier.UNDECLARED
				),
				new JavaMethod("update", void.class,
						List.of(new JavaParameter(
								new JavaClass(ArrayList.class, List.of(new JavaClass(String.class))),
								"params"
						)),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getActivatedMods",
						new JavaClass(ArrayList.class, List.of(new JavaClass(String.class))),
						new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.STATIC)
				),
				new JavaMethod("getColor", Color[].class,
						List.of(new JavaParameter(IsoPlayer.class, "player")),
						new MemberModifier(AccessModifierKey.PUBLIC)
				)
		);
		Set<JavaMethod> compiledMethods = JavaCompiler.compileJavaMethods(CompileTest.class, document);
		Assertions.assertEquals(expectedJavaMethods, compiledMethods);
	}
}
