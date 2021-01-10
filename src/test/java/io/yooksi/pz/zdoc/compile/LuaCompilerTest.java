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

import java.util.*;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.doc.ZomboidJavaDoc;
import io.yooksi.pz.zdoc.doc.ZomboidLuaDoc;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.element.lua.*;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;

public class LuaCompilerTest {

	private static final MemberModifier MODIFIER = new MemberModifier(
			AccessModifierKey.PUBLIC, ModifierKey.FINAL
	);
	private static final JavaClass JAVA_ARRAY_LIST_OBJECT = new JavaClass(
			ArrayList.class, List.of(new JavaClass(java.lang.Object.class))
	);
	private static final JavaClass JAVA_ARRAY_LIST_STRING_OBJECT = new JavaClass(
			ArrayList.class, List.of(new JavaClass(java.lang.String.class),
			new JavaClass(java.lang.Object.class))
	);
	private static final JavaClass JAVA_ARRAY_LIST_OBJECT_STRING = new JavaClass(
			ArrayList.class, List.of(new JavaClass(Object.class), new JavaClass(String.class))
	);
	private static final LuaType LUA_ARRAY_LIST_OBJECT = new LuaType(
			"ArrayList", List.of(new LuaType("Object"))
	);
	private static final LuaType LUA_ARRAY_LIST_STRING_OBJECT = new LuaType(
			"ArrayList", List.of(new LuaType("String"), new LuaType("Object"))
	);
	private static final LuaType LUA_ARRAY_LIST_OBJECT_STRING = new LuaType(
			"ArrayList", List.of(
			new LuaType("LuaCompilerTest.Object"),
			new LuaType("LuaCompilerTest.String"))
	);
	private static final LuaType LUA_ARRAY_LIST_UNKNOWN = new LuaType(
			"ArrayList", List.of(new LuaType("Unknown"))
	);
	private static final JavaClass JAVA_ARRAY_LIST_NULL;

	static
	{
		List<JavaClass> noTypeParams = new ArrayList<>();
		noTypeParams.add(null);
		JAVA_ARRAY_LIST_NULL = new JavaClass(ArrayList.class, noTypeParams);
	}

	@TestOnly
	private static Set<ZomboidLuaDoc> compileLua(Set<ZomboidJavaDoc> zJavaDocs) throws CompilerException {

		Set<ZomboidLuaDoc> result = new LuaCompiler(zJavaDocs).compile();
		Iterator<ZomboidLuaDoc> iterator = result.iterator();
		while (iterator.hasNext())
		{
			ZomboidLuaDoc doc = iterator.next();
			if (doc.getClazz().getName().equals("Unknown"))
			{
				iterator.remove();
				break;
			}
		}
		return result;
	}

	@TestOnly
	private static Set<ZomboidLuaDoc> compileLua(ZomboidJavaDoc zDoc) throws CompilerException {
		return compileLua(Set.of(zDoc));
	}

	@Test
	void shouldAvoidDuplicateClassNamesWhenCompilingLua() throws CompilerException {

		final Class<?>[] classObjects = {
				java.lang.Object.class, java.lang.String.class, java.lang.Integer.class,
				io.yooksi.pz.zdoc.compile.test.Object.class,
				io.yooksi.pz.zdoc.compile.test.String.class,
				io.yooksi.pz.zdoc.compile.test.Integer.class
		};
		Set<ZomboidJavaDoc> zJavaDocs = new LinkedHashSet<>();
		for (Class<?> c : classObjects) {
			zJavaDocs.add(new ZomboidJavaDoc(new JavaClass(c), new ArrayList<>(), new HashSet<>()));
		}
		Map<java.lang.String, java.lang.String> classData = Map.of(
				"Object", "java.lang.Object",
				"String", "java.lang.String",
				"Integer", "java.lang.Integer",
				"test_Object", "io.yooksi.pz.zdoc.compile.test.Object",
				"test_String", "io.yooksi.pz.zdoc.compile.test.String",
				"test_Integer", "io.yooksi.pz.zdoc.compile.test.Integer"
		);
		Set<LuaClass> expectedLuaClasses = new LinkedHashSet<>();
		classData.forEach((k, v) -> expectedLuaClasses.add(new LuaClass(k, v)));

		Set<LuaClass> actualLuaClasses = new HashSet<>();
		compileLua(zJavaDocs).forEach(doc -> actualLuaClasses.add((LuaClass) doc.getClazz()));

		Assertions.assertEquals(expectedLuaClasses, actualLuaClasses);
	}

	@Test
	void shouldCorrectlyFormatInnerClassNamesWhenCompilingLua() throws CompilerException {

		final Class<?>[] classObjects = {
				Object.class, String.class, Integer.class
		};
		Set<ZomboidJavaDoc> zJavaDocs = new LinkedHashSet<>();
		for (Class<?> c : classObjects) {
			zJavaDocs.add(new ZomboidJavaDoc(new JavaClass(c), new ArrayList<>(), new HashSet<>()));
		}
		Map<java.lang.String, java.lang.String> classData = Map.of(
				"LuaCompilerTest.Object", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.Object",
				"LuaCompilerTest.String", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.String",
				"LuaCompilerTest.Integer", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.Integer"
		);
		Set<LuaClass> expectedLuaClasses = new LinkedHashSet<>();
		classData.forEach((k, v) -> expectedLuaClasses.add(new LuaClass(k, v)));

		Set<LuaClass> actualLuaClasses = new HashSet<>();
		compileLua(zJavaDocs).forEach(doc -> actualLuaClasses.add((LuaClass) doc.getClazz()));

		Assertions.assertEquals(expectedLuaClasses, actualLuaClasses);
	}

	@Test
	void shouldCompileLuaDocsWithValidClassFromZomboidJavaDocs() throws CompilerException {

		LuaClass expectedClass = new LuaClass(
				LuaCompilerTest.class.getSimpleName(), LuaCompilerTest.class.getName()
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		Assertions.assertEquals(expectedClass, zLuaDocs.iterator().next().getClazz());
	}

	@Test
	void shouldCompileValidLuaFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = List.of(
				new JavaField(java.lang.Object.class, "object", MODIFIER),
				new JavaField(java.lang.String.class, "text", MODIFIER),
				new JavaField(java.lang.Integer.class, "num", MODIFIER)
		);
		List<LuaField> expectedFields = List.of(
				new LuaField(new LuaType("Object"), "object", MODIFIER),
				new LuaField(new LuaType("String"), "text", MODIFIER),
				new LuaField(new LuaType("Integer"), "num", MODIFIER)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), javaFields, new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedFields, zLuaDoc.getFields());
	}

	@Test
	void shouldCompileValidLuaArrayFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = List.of(
				new JavaField(java.lang.Object[].class, "object", MODIFIER),
				new JavaField(java.lang.String[].class, "text", MODIFIER),
				new JavaField(java.lang.Integer[].class, "num", MODIFIER)
		);
		List<LuaField> expectedFields = List.of(
				new LuaField(new LuaType("Object[]"), "object", MODIFIER),
				new LuaField(new LuaType("String[]"), "text", MODIFIER),
				new LuaField(new LuaType("Integer[]"), "num", MODIFIER)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), javaFields, new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedFields, zLuaDoc.getFields());
	}

	@Test
	void shouldCompileValidLuaParameterizedTypeFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = List.of(
				new JavaField(JAVA_ARRAY_LIST_OBJECT, "object", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_STRING_OBJECT, "text", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_OBJECT_STRING, "text", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_NULL, "num", MODIFIER)
		);
		List<LuaField> expectedFields = List.of(
				new LuaField(LUA_ARRAY_LIST_OBJECT, "object", MODIFIER),
				new LuaField(LUA_ARRAY_LIST_STRING_OBJECT, "text", MODIFIER),
				new LuaField(LUA_ARRAY_LIST_OBJECT_STRING, "text", MODIFIER),
				new LuaField(LUA_ARRAY_LIST_UNKNOWN, "num", MODIFIER)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), javaFields, new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedFields, zLuaDoc.getFields());
	}

	@Test
	void shouldCompileValidLuaMethodsFromZomboidJavaDocs() throws CompilerException {

		LuaClass ownerClass = new LuaClass(
				LuaCompilerTest.class.getSimpleName(), LuaCompilerTest.class.getName()
		);
		Set<JavaMethod> javaMethods = Set.of(
				new JavaMethod("getText", java.lang.String.class, List.of(
						new JavaParameter(java.lang.Integer.class, "iParam")), MODIFIER
				),
				new JavaMethod("getNumber", java.lang.Integer.class, List.of(
						new JavaParameter(java.lang.Object.class, "oParam")), MODIFIER
				),
				new JavaMethod("getObject", java.lang.Object.class, List.of(
						new JavaParameter(java.lang.String.class, "sParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = Set.of(
				new LuaMethod("getText", ownerClass, MODIFIER, new LuaType("String"),
						List.of(new LuaParameter(new LuaType("Integer"), "iParam"))
				),
				new LuaMethod("getNumber", ownerClass, MODIFIER, new LuaType("Integer"),
						List.of(new LuaParameter(new LuaType("Object"), "oParam"))
				),
				new LuaMethod("getObject", ownerClass, MODIFIER, new LuaType("Object"),
						List.of(new LuaParameter(new LuaType("String"), "sParam"))
				)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@Test
	void shouldCompileValidLuaMethodsWithArraysFromZomboidJavaDocs() throws CompilerException {

		LuaClass ownerClass = new LuaClass(
				LuaCompilerTest.class.getSimpleName(), LuaCompilerTest.class.getName()
		);
		Set<JavaMethod> javaMethods = Set.of(
				new JavaMethod("getText", java.lang.String[].class, List.of(
						new JavaParameter(java.lang.Integer[].class, "iParam")), MODIFIER
				),
				new JavaMethod("getNumber", java.lang.Integer[].class, List.of(
						new JavaParameter(java.lang.Object[].class, "oParam")), MODIFIER
				),
				new JavaMethod("getObject", java.lang.Object[].class, List.of(
						new JavaParameter(java.lang.String[].class, "sParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = Set.of(
				new LuaMethod("getText", ownerClass, MODIFIER, new LuaType("String[]"),
						List.of(new LuaParameter(new LuaType("Integer[]"), "iParam"))
				),
				new LuaMethod("getNumber", ownerClass, MODIFIER, new LuaType("Integer[]"),
						List.of(new LuaParameter(new LuaType("Object[]"), "oParam"))
				),
				new LuaMethod("getObject", ownerClass, MODIFIER, new LuaType("Object[]"),
						List.of(new LuaParameter(new LuaType("String[]"), "sParam"))
				)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@Test
	void shouldCompileValidLuaMethodsWithParameterizedTypesFromZomboidJavaDocs() throws CompilerException {

		LuaClass ownerClass = new LuaClass(
				LuaCompilerTest.class.getSimpleName(), LuaCompilerTest.class.getName()
		);
		Set<JavaMethod> javaMethods = Set.of(
				new JavaMethod("getText", JAVA_ARRAY_LIST_OBJECT, List.of(
						new JavaParameter(JAVA_ARRAY_LIST_STRING_OBJECT, "sParam")), MODIFIER
				),
				new JavaMethod("getNumber", JAVA_ARRAY_LIST_STRING_OBJECT, List.of(
						new JavaParameter(JAVA_ARRAY_LIST_OBJECT_STRING, "nParam")), MODIFIER
				),
				new JavaMethod("getObject", JAVA_ARRAY_LIST_OBJECT_STRING, List.of(
						new JavaParameter(JAVA_ARRAY_LIST_NULL, "oParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = Set.of(
				new LuaMethod("getText", ownerClass, MODIFIER, LUA_ARRAY_LIST_OBJECT,
						List.of(new LuaParameter(LUA_ARRAY_LIST_STRING_OBJECT, "sParam"))
				),
				new LuaMethod("getNumber", ownerClass, MODIFIER, LUA_ARRAY_LIST_STRING_OBJECT,
						List.of(new LuaParameter(LUA_ARRAY_LIST_OBJECT_STRING, "nParam"))
				),
				new LuaMethod("getObject", ownerClass, MODIFIER, LUA_ARRAY_LIST_OBJECT_STRING,
						List.of(new LuaParameter(LUA_ARRAY_LIST_UNKNOWN, "oParam"))
				)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@TestOnly
	private static class Object {
	}

	@TestOnly
	private static class String {
	}

	@TestOnly
	private static class Integer {
	}
}
