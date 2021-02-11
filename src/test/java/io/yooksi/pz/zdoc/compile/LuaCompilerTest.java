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
package io.yooksi.pz.zdoc.compile;

import java.util.*;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LuaCompilerTest {

	private static final LuaClass OWNER_CLASS = new LuaClass(
			LuaCompilerTest.class.getSimpleName(), LuaCompilerTest.class.getName()
	);
	private static final MemberModifier MODIFIER = new MemberModifier(
			AccessModifierKey.PUBLIC, ModifierKey.FINAL
	);
	private static final JavaClass JAVA_ARRAY_LIST_OBJECT = new JavaClass(
			ArrayList.class, ImmutableList.of(new JavaClass(java.lang.Object.class))
	);
	private static final JavaClass JAVA_ARRAY_LIST_STRING_OBJECT = new JavaClass(
			ArrayList.class, ImmutableList.of(new JavaClass(java.lang.String.class),
			new JavaClass(java.lang.Object.class))
	);
	private static final JavaClass JAVA_ARRAY_LIST_OBJECT_STRING = new JavaClass(
			ArrayList.class, ImmutableList.of(new JavaClass(Object.class), new JavaClass(String.class))
	);
	private static final LuaType LUA_ARRAY_LIST_OBJECT = new LuaType(
			"ArrayList", ImmutableList.of(new LuaType("Object"))
	);
	private static final LuaType LUA_ARRAY_LIST_STRING_OBJECT = new LuaType(
			"ArrayList", ImmutableList.of(new LuaType("String"), new LuaType("Object"))
	);
	private static final LuaType LUA_ARRAY_LIST_OBJECT_STRING = new LuaType(
			"ArrayList", ImmutableList.of(
			new LuaType("LuaCompilerTest.Object"),
			new LuaType("LuaCompilerTest.String"))
	);
	private static final LuaType LUA_ARRAY_LIST_UNKNOWN = new LuaType(
			"ArrayList", ImmutableList.of(new LuaType("Unknown"))
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
		return new LuaCompiler(zJavaDocs).compile();
	}

	@TestOnly
	private static Set<ZomboidLuaDoc> compileLua(ZomboidJavaDoc zDoc) throws CompilerException {
		return compileLua(Collections.singleton(zDoc));
	}

	@Test @Order(1)
	void shouldCorrectlyFormatInnerClassNamesWhenCompilingLua() throws CompilerException {

		final Class<?>[] classObjects = {
				Object.class, String.class, Integer.class
		};
		Set<ZomboidJavaDoc> zJavaDocs = new LinkedHashSet<>();
		for (Class<?> c : classObjects) {
			zJavaDocs.add(new ZomboidJavaDoc(new JavaClass(c), new ArrayList<>(), new HashSet<>()));
		}
		Map<java.lang.String, java.lang.String> classData = ImmutableMap.of(
				"LuaCompilerTest.Object", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.Object",
				"LuaCompilerTest.String", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.String",
				"LuaCompilerTest.Integer", "io.yooksi.pz.zdoc.compile.LuaCompilerTest.Integer"
		);
		Set<LuaClass> expectedLuaClasses = new LinkedHashSet<>();
		classData.forEach((k, v) -> expectedLuaClasses.add(new LuaClass(k, v)));

		Set<LuaClass> actualLuaClasses = new HashSet<>();
		compileLua(zJavaDocs).forEach(doc -> actualLuaClasses.add(doc.getClazz()));

		Assertions.assertEquals(expectedLuaClasses, actualLuaClasses);
	}

	@Test @Order(2)
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

	@Test @Order(3)
	void shouldCompileValidLuaFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = ImmutableList.of(
				new JavaField(java.lang.Object.class, "object", MODIFIER),
				new JavaField(java.lang.String.class, "text", MODIFIER),
				new JavaField(java.lang.Integer.class, "num", MODIFIER)
		);
		List<LuaField> expectedFields = ImmutableList.of(
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

	@Test @Order(4)
	void shouldCompileValidLuaArrayFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = ImmutableList.of(
				new JavaField(java.lang.Object[].class, "objectArray1", MODIFIER),
				new JavaField(java.lang.String[].class, "textArray1", MODIFIER),
				new JavaField(java.lang.Integer[].class, "numArray1", MODIFIER),
				new JavaField(java.lang.Object[][].class, "objectArray2", MODIFIER),
				new JavaField(java.lang.String[][][].class, "textArray2", MODIFIER),
				new JavaField(java.lang.Integer[][][][].class, "numArray2", MODIFIER),
				new JavaField(int[].class, "intArray1", MODIFIER),
				new JavaField(boolean[].class, "booleanArray1", MODIFIER),
				new JavaField(char[].class, "charArray1", MODIFIER),
				new JavaField(int[][].class, "intArray2", MODIFIER),
				new JavaField(boolean[][][].class, "booleanArray2", MODIFIER),
				new JavaField(char[][][][].class, "charArray2", MODIFIER)
		);
		List<LuaField> expectedFields = ImmutableList.of(
				new LuaField(new LuaType("Object[]"), "objectArray1", MODIFIER),
				new LuaField(new LuaType("String[]"), "textArray1", MODIFIER),
				new LuaField(new LuaType("Integer[]"), "numArray1", MODIFIER),
				new LuaField(new LuaType("Object[][]"), "objectArray2", MODIFIER),
				new LuaField(new LuaType("String[][][]"), "textArray2", MODIFIER),
				new LuaField(new LuaType("Integer[][][][]"), "numArray2", MODIFIER),
				new LuaField(new LuaType("int[]"), "intArray1", MODIFIER),
				new LuaField(new LuaType("boolean[]"), "booleanArray1", MODIFIER),
				new LuaField(new LuaType("char[]"), "charArray1", MODIFIER),
				new LuaField(new LuaType("int[][]"), "intArray2", MODIFIER),
				new LuaField(new LuaType("boolean[][][]"), "booleanArray2", MODIFIER),
				new LuaField(new LuaType("char[][][][]"), "charArray2", MODIFIER)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), javaFields, new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedFields, zLuaDoc.getFields());
	}

	@Test @Order(5)
	void shouldCompileValidLuaParameterizedTypeFieldsFromZomboidJavaDocs() throws CompilerException {

		List<JavaField> javaFields = ImmutableList.of(
				new JavaField(JAVA_ARRAY_LIST_OBJECT, "object", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_STRING_OBJECT, "text", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_OBJECT_STRING, "text", MODIFIER),
				new JavaField(JAVA_ARRAY_LIST_NULL, "num", MODIFIER)
		);
		List<LuaField> expectedFields = ImmutableList.of(
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

		List<LuaField> actualFields = zLuaDocs.iterator().next().getFields();
		Assertions.assertEquals(expectedFields, actualFields);
	}

	@Test @Order(6)
	void shouldCompileValidLuaMethodsFromZomboidJavaDocs() throws CompilerException {

		Set<JavaMethod> javaMethods = ImmutableSet.of(
				new JavaMethod("getText", java.lang.String.class, ImmutableList.of(
						new JavaParameter(java.lang.Integer.class, "iParam")), MODIFIER
				),
				new JavaMethod("getNumber", java.lang.Integer.class, ImmutableList.of(
						new JavaParameter(java.lang.Object.class, "oParam")), MODIFIER
				),
				new JavaMethod("getObject", java.lang.Object.class, ImmutableList.of(
						new JavaParameter(java.lang.String.class, "sParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = ImmutableSet.of(
				LuaMethod.Builder.create("getText").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("String")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("Integer"), "iParam"))).build(),

				LuaMethod.Builder.create("getNumber").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("Integer")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("Object"), "oParam"))).build(),

				LuaMethod.Builder.create("getObject").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("Object")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("String"), "sParam"))).build()
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@Test @Order(7)
	void shouldCompileValidLuaMethodsWithArraysFromZomboidJavaDocs() throws CompilerException {

		Set<JavaMethod> javaMethods = ImmutableSet.of(
				new JavaMethod("getFloat", java.lang.Float[].class, ImmutableList.of(
						new JavaParameter(java.lang.Integer[].class, "iParam")), MODIFIER
				),
				new JavaMethod("getDouble", java.lang.Double[].class, ImmutableList.of(
						new JavaParameter(java.lang.Object[].class, "oParam")), MODIFIER
				),
				new JavaMethod("getByte", java.lang.Byte[].class, ImmutableList.of(
						new JavaParameter(java.lang.String[].class, "sParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = ImmutableSet.of(
				LuaMethod.Builder.create("getFloat").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("Float[]")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("Integer[]"), "iParam"))).build(),

				LuaMethod.Builder.create("getDouble").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("Double[]")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("Object[]"), "oParam"))).build(),

				LuaMethod.Builder.create("getByte").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(new LuaType("Byte[]")).withParams(
						ImmutableList.of(new LuaParameter(new LuaType("String[]"), "sParam"))).build()
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@Test @Order(8)
	void shouldNotRegisterArrayGlobalTypesWhenCompilingLuaMethodsFromZomboidJavaDocs() {

		for (LuaClass globalType : LuaCompiler.getGlobalTypes()) {
			Assertions.assertFalse(globalType.getName().contains("[]"));
		}
	}

	@Test @Order(9)
	void shouldCompileValidLuaMethodsWithParameterizedTypesFromZomboidJavaDocs() throws CompilerException {

		Set<JavaMethod> javaMethods = ImmutableSet.of(
				new JavaMethod("getText", JAVA_ARRAY_LIST_OBJECT, ImmutableList.of(
						new JavaParameter(JAVA_ARRAY_LIST_STRING_OBJECT, "sParam")), MODIFIER
				),
				new JavaMethod("getNumber", JAVA_ARRAY_LIST_STRING_OBJECT, ImmutableList.of(
						new JavaParameter(JAVA_ARRAY_LIST_OBJECT_STRING, "nParam")), MODIFIER
				),
				new JavaMethod("getObject", JAVA_ARRAY_LIST_OBJECT_STRING, ImmutableList.of(
						new JavaParameter(JAVA_ARRAY_LIST_NULL, "oParam")), MODIFIER
				)
		);
		Set<LuaMethod> expectedMethods = ImmutableSet.of(
				LuaMethod.Builder.create("getText").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(LUA_ARRAY_LIST_OBJECT).withParams(
						ImmutableList.of(new LuaParameter(LUA_ARRAY_LIST_STRING_OBJECT, "sParam"))).build(),

				LuaMethod.Builder.create("getNumber").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(LUA_ARRAY_LIST_STRING_OBJECT).withParams(
						ImmutableList.of(new LuaParameter(LUA_ARRAY_LIST_OBJECT_STRING, "nParam"))).build(),

				LuaMethod.Builder.create("getObject").withOwner(OWNER_CLASS)
						.withModifier(MODIFIER).withReturnType(LUA_ARRAY_LIST_OBJECT_STRING).withParams(
						ImmutableList.of(new LuaParameter(LUA_ARRAY_LIST_UNKNOWN, "oParam"))).build()
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), new ArrayList<>(), javaMethods
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();
		Assertions.assertEquals(expectedMethods, zLuaDoc.getMethods());
	}

	@Test @Order(10)
	void shouldIncludeCommentsWhenCompilingLua() throws CompilerException {

		List<JavaField> fieldsWithComment = ImmutableList.of(
				new JavaField(new JavaClass(java.lang.Object.class), "object",
						MODIFIER, "this field has a comment")
		);
		Set<JavaMethod> methodsWithComment = ImmutableSet.of(
				new JavaMethod("getText", new JavaClass(java.lang.String.class), ImmutableList.of(),
						MODIFIER, false, "this method has a comment"
				)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), fieldsWithComment, methodsWithComment
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());
		ZomboidLuaDoc zLuaDoc = zLuaDocs.iterator().next();

		java.lang.String actual = zLuaDoc.getFields().iterator().next().getComment();
		Assertions.assertEquals("this field has a comment", actual);

		actual = zLuaDoc.getMethods().iterator().next().getComment();
		Assertions.assertEquals("this method has a comment", actual);
	}

	@Test @Order(11)
	void shouldGatherAllGlobalTypesWhenCompilingLua() {

		Set<LuaClass> expectedGlobalTypes = Sets.newHashSet(
				new LuaClass("Object", "java.lang.Object"),
				new LuaClass("String", "java.lang.String"),
				new LuaClass("Integer", "java.lang.Integer"),
				new LuaClass("Double", "java.lang.Double"),
				new LuaClass("Float", "java.lang.Float"),
				new LuaClass("Byte", "java.lang.Byte"),
				new LuaClass("int"),
				new LuaClass("boolean"),
				new LuaClass("char"),
				new LuaClass("ArrayList", "java.util.ArrayList"),
				new LuaClass("Unknown")
		);
		Set<LuaClass> actualGlobalTypes = LuaCompiler.getGlobalTypes();
		Assertions.assertEquals(expectedGlobalTypes, actualGlobalTypes);
	}

	@Test @Order(12)
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
		Map<java.lang.String, java.lang.String> classData =
				ImmutableMap.<java.lang.String, java.lang.String>builder()
						.put("Object", "java.lang.Object")
						.put("String", "java.lang.String")
						.put("Integer", "java.lang.Integer")
						.put("test_Object", "io.yooksi.pz.zdoc.compile.test.Object")
						.put("test_String", "io.yooksi.pz.zdoc.compile.test.String")
						.put("test_Integer", "io.yooksi.pz.zdoc.compile.test.Integer").build();

		Set<LuaClass> expectedLuaClasses = new LinkedHashSet<>();
		classData.forEach((k, v) -> expectedLuaClasses.add(new LuaClass(k, v)));

		Set<LuaClass> actualLuaClasses = new HashSet<>();
		compileLua(zJavaDocs).forEach(doc -> actualLuaClasses.add(doc.getClazz()));

		Assertions.assertEquals(expectedLuaClasses, actualLuaClasses);
	}

	@Test @Order(13)
	void shouldAvoidDuplicateTypeNamesWhenCompilingLua() throws CompilerException {

		MemberModifier modifier = MemberModifier.UNDECLARED;
		List<JavaField> javaFields = ImmutableList.of(
				new JavaField(new JavaClass(java.lang.Object.class),
						"object1", modifier
				),
				new JavaField(new JavaClass(java.lang.String.class),
						"string1", modifier
				),
				new JavaField(new JavaClass(java.lang.Integer.class),
						"integer1", modifier
				),
				new JavaField(new JavaClass(io.yooksi.pz.zdoc.compile.test.Object.class),
						"object2", modifier
				),
				new JavaField(new JavaClass(io.yooksi.pz.zdoc.compile.test.String.class),
						"string2", modifier
				),
				new JavaField(new JavaClass(io.yooksi.pz.zdoc.compile.test.Integer.class),
						"integer2", modifier)
		);
		List<LuaField> luaFields = ImmutableList.of(
				new LuaField(new LuaType("Object"), "object1", modifier
				),
				new LuaField(new LuaType("String"), "string1", modifier
				),
				new LuaField(new LuaType("Integer"), "integer1", modifier
				),
				new LuaField(new LuaType("test_Object"), "object2", modifier
				),
				new LuaField(new LuaType("test_String"), "string2", modifier
				),
				new LuaField(new LuaType("test_Integer"), "integer2", modifier)
		);
		ZomboidJavaDoc zJavaDoc = new ZomboidJavaDoc(
				new JavaClass(LuaCompilerTest.class), javaFields, new HashSet<>()
		);
		Set<ZomboidLuaDoc> zLuaDocs = compileLua(zJavaDoc);
		Assertions.assertEquals(1, zLuaDocs.size());

		Assertions.assertEquals(luaFields, zLuaDocs.iterator().next().getFields());
	}

	@Test @Order(13)
	void shouldFilterGlobalClassTypesFromGlobalTypesWhenCompilingLua() {

		Set<LuaClass> expectedGlobalTypes = Sets.newHashSet(
				new LuaClass("int"),
				new LuaClass("boolean"),
				new LuaClass("char"),
				new LuaClass("Double", "java.lang.Double"),
				new LuaClass("Float", "java.lang.Float"),
				new LuaClass("Byte", "java.lang.Byte"),
				new LuaClass("ArrayList", "java.util.ArrayList"),
				new LuaClass("Unknown")
		);
		Set<LuaClass> actualGlobalTypes = LuaCompiler.getGlobalTypes();
		Assertions.assertEquals(expectedGlobalTypes, actualGlobalTypes);
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
