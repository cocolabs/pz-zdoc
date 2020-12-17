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
package io.yooksi.pz.zdoc.parser;

import io.yooksi.pz.zdoc.TestWorkspace;
import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.doc.LuaDoc;
import io.yooksi.pz.zdoc.element.JavaMethod;
import io.yooksi.pz.zdoc.element.LuaMethod;
import io.yooksi.pz.zdoc.element.Method;
import io.yooksi.pz.zdoc.element.JavaField;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

public class JavaDocParserTest extends TestWorkspace implements UnitTest {

	private static JavaDocParser sampleJavaDocParser;

	public JavaDocParserTest() {
		super("outputLua.lua");
	}

	@BeforeAll
	static void initializeStaticParsers() throws IOException {
		sampleJavaDocParser = JavaDocParser.create(Paths.get("src/test/resources/Sample.html"));
	}

	@Test
	void shouldCorrectlyParseJavaDocMethod() {

		List<JavaMethod> methods = sampleJavaDocParser.parse().getMethods();
		Assertions.assertEquals(5, methods.size());

		String[] methodName = {
				"begin", "init", "IsFinished", "update", "getActivatedMods"
		};
		for (int i = 0; i < methods.size(); i++) {
			Assertions.assertEquals(methodName[i], methods.get(i).getName());
		}
	}

	@Test
	void shouldCorrectlyParseEmptyMethodParams() {

		List<JavaMethod> methods = sampleJavaDocParser.parse().getMethods();

		JavaField[] params = methods.get(0).getParams();
		Assertions.assertEquals(0, params.length);
	}

	@Test
	void shouldGenerateValidLuaMethodDocumentation() {

		List<JavaMethod> methods = sampleJavaDocParser.parse().getMethods();
		List<String> luaDoc = LuaMethod.Parser.create(methods.get(1)).parse().annotate();

		String[] expectedDoc = {
				"---@param object string",
				"---@param params string[]",
				"---@return void"
		};
		for (int i = 0; i < luaDoc.size(); i++) {
			Assertions.assertEquals(expectedDoc[i], luaDoc.get(i));
		}
	}

	@Test
	void shouldCorrectlyConvertJavaToLuaDocumentation() throws IOException {

		sampleJavaDocParser.parse().compileLuaLibrary(true, false).writeToFile(file.toPath());

		List<String> output = FileUtils.readLines(file, Charset.defaultCharset());
		String[] actual = output.toArray(new String[]{});

		String[] expected = {
				"---@return void",
				"function begin() end",
				"",
				"---@param object string",
				"---@param params string[]",
				"---@return void",
				"function init(object, params) end",
				"",
				"---@return boolean",
				"function IsFinished() end",
				"",
				"---@return void",
				"function update() end",
		};
		for (int i = 0; i < expected.length; i++) {
			Assertions.assertEquals(expected[i], actual[i]);
		}
	}

	@Test
	void shouldNotParsePrivateMethodsFromJavaDocs() {

		List<JavaMethod> methods = sampleJavaDocParser.parse().getMethods();
		Assertions.assertEquals(5, methods.size());
		Assertions.assertEquals("init", methods.get(1).getName());
	}

	@Test
	void shouldQualifyMethodsWhenConvertingToLuaDoc() {

		LuaDoc luaDoc = sampleJavaDocParser.parse().compileLuaLibrary(false, true);
		for (Method method : luaDoc.getMethods()) {
			Assertions.assertTrue(method.toString().startsWith("function Sample:"));
		}
	}

	@Test
	void shouldCorrectlyParseObjectType() {

		LuaDoc luaDoc = sampleJavaDocParser.parse().compileLuaLibrary(true, false);
		Method method = luaDoc.getMethods().get(4);
		String[] expected = {
				"---@return ArrayList<String>",
				"function getActivatedMods() end"
		};
		String[] methodContent = method.toString().split("\n");
		for (int i = 0; i < methodContent.length; i++) {
			Assertions.assertEquals(expected[i], methodContent[i]);
		}
	}
}
