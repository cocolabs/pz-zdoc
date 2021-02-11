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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.yooksi.pz.zdoc.Main;
import io.yooksi.pz.zdoc.TestWorkspace;

import static io.yooksi.pz.zdoc.compile.LuaAnnotator.AnnotateResult;
import static io.yooksi.pz.zdoc.compile.LuaAnnotator.AnnotateRules;

class LuaAnnotatorTest extends TestWorkspace {

	private static final ClassLoader CL = LuaAnnotatorTest.class.getClassLoader();
	private static final File INCLUSION_TEST, NO_MATCH_TEST, EXPECTED_INCLUSION;

	static
	{
		try {
			INCLUSION_TEST = new File(
					Objects.requireNonNull(CL.getResource("LuaInclusionTest.lua")).toURI()
			);
			NO_MATCH_TEST = new File(
					Objects.requireNonNull(CL.getResource("LuaNoMatchTest.lua")).toURI()
			);
			EXPECTED_INCLUSION = new File(
					Objects.requireNonNull(CL.getResource("LuaExpectedInclusion.lua")).toURI()
			);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void shouldMatchLuaTableDeclarationIndentationWithRegex() {

		Map<String, String> luaTableDeclarations = ImmutableMap.of(
				"   ", "   NewTestTable1 = ISTestTable:new(\"TestTable\")",
				"		", "		NewTestTable1 = ISTestTable:new(\"TestTable\")"
		);
		for (Map.Entry<String, String> entry : luaTableDeclarations.entrySet())
		{
			Matcher matcher = LuaAnnotator.LUA_TABLE_DECLARATION.matcher(entry.getValue());
			Assertions.assertTrue(matcher.find());
			Assertions.assertEquals(entry.getKey(), matcher.group(1));
		}
		String declarationWithNoIndentation = "NewTestTable1 = ISTestTable:new(\"TestTable\")";
		Matcher matcher = LuaAnnotator.LUA_TABLE_DECLARATION.matcher(declarationWithNoIndentation);

		Assertions.assertTrue(matcher.find());
		Assertions.assertNull(matcher.group(1));
	}

	@Test
	void shouldMatchLuaTableDeclarationWithRegex() {

		Map<String, String> luaTableDeclarations = ImmutableMap.<String, String>builder()
				.put("NewTestTable0", "NewTestTable0 = ISTestTable:new()")
				.put("NewTestTable1", "NewTestTable1 = ISTestTable:new(\"TestTable\")")
				.put("NewTestTable2", "NewTestTable2=ISTestTable:new(\"TestTable\")")
				.put("DerivedTestTable0", "DerivedTestTable0 = ISTestTable:derive()")
				.put("DerivedTestTable1", "DerivedTestTable1 = ISTestTable:derive(\"TestTable\")")
				.put("DerivedTestTable2", "DerivedTestTable2=ISTestTable:derive(\"TestTable\")")
				.put("DeclaredTestTable0", "DeclaredTestTable0 = ISTestTable or {}")
				.put("DeclaredTestTable1", "DeclaredTestTable1 = {}")
				.put("DeclaredTestTable2", "DeclaredTestTable2={ }").build();

		for (Map.Entry<String, String> entry : luaTableDeclarations.entrySet())
		{
			Matcher matcher = LuaAnnotator.LUA_TABLE_DECLARATION.matcher(entry.getValue());
			Assertions.assertTrue(matcher.find());
			Assertions.assertEquals(entry.getKey(), matcher.group(2));
		}
	}

	@Test
	void shouldMatchLuaNewOrDerivedClassDeclarationWithRegex() {

		Map<String, String> luaTableDeclarations = ImmutableMap.of(
				"new", "NewTestTable = ISTestTable:new(\"TestTable\")",
				"derive", "DerivedTestTable = ISTestTable:derive(\"TestTable\")"
		);
		for (Map.Entry<String, String> entry : luaTableDeclarations.entrySet())
		{
			Matcher matcher = LuaAnnotator.LUA_TABLE_DECLARATION.matcher(entry.getValue());
			Assertions.assertTrue(matcher.find());
			Assertions.assertEquals("ISTestTable", matcher.group(3));
			Assertions.assertEquals(entry.getKey(), matcher.group(4));
		}
		Map<String, String> badLuaTableDeclarations = ImmutableMap.of(
				"new", "NewTestTable = ISTestTable:newly(\"TestTable\")",
				"derive", "DerivedTestTable = ISTestTable:derived(\"TestTable\")"
		);
		for (Map.Entry<String, String> entry : badLuaTableDeclarations.entrySet())
		{
			Matcher matcher = LuaAnnotator.LUA_TABLE_DECLARATION.matcher(entry.getValue());
			Assertions.assertTrue(matcher.find());
			Assertions.assertNull(matcher.group(4));
		}
	}

	@Test
	void shouldThrowExceptionWhenTryingToAnnotateNonExistingFile() {

		Assertions.assertThrows(FileNotFoundException.class, () ->
				LuaAnnotator.annotate(new File("nonExistingFile"), new ArrayList<>())
		);
	}

	@Test
	void shouldThrowExceptionWhenTryingToAnnotateWithImmutableExcludeRules() {

		AnnotateRules rules = new AnnotateRules(ImmutableSet.of());
		Assertions.assertThrows(UnsupportedOperationException.class, () ->
				LuaAnnotator.annotate(INCLUSION_TEST, new ArrayList<>(), rules)
		);
	}

	@Test
	void shouldSkipAnnotatingWhenTyringToAnnotateEmptyFile() throws IOException {

		List<String> content = new ArrayList<>();
		AnnotateResult result = LuaAnnotator.annotate(file, content);

		Assertions.assertEquals(AnnotateResult.SKIPPED_FILE_EMPTY, result);
		Assertions.assertTrue(content.isEmpty());
	}

	@Test
	void shouldSkipAnnotatingElementsWhenMatchedExcludeRules() throws IOException {

		List<String> content = new ArrayList<>();
		AnnotateRules rules = new AnnotateRules(ImmutableSet.of("LuaInclusionTest"));
		AnnotateResult result = LuaAnnotator.annotate(INCLUSION_TEST, content, rules);

		Assertions.assertEquals(AnnotateResult.ALL_EXCLUDED, result);
		Assertions.assertEquals(content, FileUtils.readLines(INCLUSION_TEST, Main.CHARSET));
	}

	@Test
	void shouldSkipAnnotatingElementsWhenFileIgnoredByRules() throws IOException {

		List<String> content = new ArrayList<>();
		Properties properties = new Properties();
		properties.put("LuaInclusionTest", "");

		AnnotateRules rules = new AnnotateRules(properties);
		AnnotateResult result = LuaAnnotator.annotate(INCLUSION_TEST, content, rules);

		Assertions.assertEquals(AnnotateResult.SKIPPED_FILE_IGNORED, result);
		Assertions.assertTrue(content.isEmpty());
	}

	@Test
	void shouldAnnotateAllElementsWhenMatchedIncludeRules() throws IOException {

		List<String> content = new ArrayList<>();
		Properties properties = new Properties();
		properties.put("LuaInclusionTest", "LuaInclusionTest,DerivedTest");

		AnnotateRules rules = new AnnotateRules(properties);
		AnnotateResult result = LuaAnnotator.annotate(INCLUSION_TEST, content, rules);

		Assertions.assertEquals(AnnotateResult.ALL_INCLUDED, result);
		List<String> expected = FileUtils.readLines(EXPECTED_INCLUSION, Main.CHARSET);
		Assertions.assertEquals(content, expected);
	}

	@Test
	void shouldAnnotateSomeElementsWhenPartialMatchedRules() throws IOException {

		List<String> content = new ArrayList<>();
		Properties properties = new Properties();
		properties.put("LuaInclusionTest", "LuaInclusionTest,DerivedTest");

		Set<String> exclude = new HashSet<>();
		exclude.add("DerivedTest");

		AnnotateRules rules = new AnnotateRules(properties, exclude);
		AnnotateResult result = LuaAnnotator.annotate(INCLUSION_TEST, content, rules);

		Assertions.assertEquals(AnnotateResult.PARTIAL_INCLUSION, result);
	}

	@Test
	void shouldAnnotateFileWithUndeclaredLuaClass() throws IOException {

		List<String> content = new ArrayList<>();
		AnnotateRules rules = new AnnotateRules();
		AnnotateResult result = LuaAnnotator.annotate(NO_MATCH_TEST, content, rules);

		Assertions.assertEquals(AnnotateResult.NO_MATCH, result);
	}
}
