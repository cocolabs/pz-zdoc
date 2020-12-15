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
package io.yooksi.pz.zdoc.doc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.TestWorkspace;
import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.*;
import io.yooksi.pz.zdoc.parser.JavaDocParser;

public class JavaDocTest extends TestWorkspace implements UnitTest {

	private static JavaDocParser sampleJavaDocParser;

	JavaDocTest() {
		super("outputLua.lua");
	}

	@BeforeAll
	static void initializeStaticParsers() throws IOException {
		sampleJavaDocParser = JavaDocParser.create(Paths.get("src/test/resources/Sample.html"));
	}

	@Test
	void shouldCorrectlyResolveApiURL() {

		String expected = "https://projectzomboid.com/modding/zombie/inventory/InventoryItem.html";
		URL actual = JavaDoc.resolveApiURL("zombie/inventory/InventoryItem.html");

		Assertions.assertEquals(expected, actual.toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingNonApiUrl() {

		Assertions.assertThrows(IllegalArgumentException.class, () ->
				JavaDoc.resolveApiURL("https://nonapiwebsite.com"));
	}

	@Test
	void shouldCorrectlyResolveApiURLFromOtherURL() {

		String link = "https://projectzomboid.com/";
		Assertions.assertEquals(link, JavaDoc.resolveApiURL(link).toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingApiURLWithInvalidArgument() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> JavaDoc.resolveApiURL('\u0000' + "/p*!h"));
	}

	@Test
	void shouldAttachMissingHTMLFileExtensionWhenParsingApiURL() throws MalformedURLException {

		URL url = new URL("https://projectzomboid.com/modding/zombie/Class.html");
		Assertions.assertEquals(url, JavaDoc.resolveApiURL("zombie/Class"));
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

		Parameter[] params = methods.get(0).getParams();
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
	void shouldCorrectlyParseJavaDocMemberClasses() throws IOException {

		Map<JavaDocParser, Class<?>> dataMap = new HashMap<>();
		dataMap.put(JavaDocParser.create(JavaDoc.API_GLOBAL_OBJECT), URL.class);
		dataMap.put(JavaDocParser.create(Paths.get("src/test/resources/Sample.html")), Path.class);

		for (Map.Entry<JavaDocParser, Class<?>> entry1 : dataMap.entrySet())
		{
			Map<String, ? extends MemberClass> map = entry1.getKey().parse().getFields();
			for (Map.Entry<String, ? extends MemberClass> entry2 : map.entrySet())
			{
				Assertions.assertFalse(entry2.getKey().isEmpty());

				MemberClass member = entry2.getValue();
				Assertions.assertTrue(member instanceof JavaClass);

				Object location = ((JavaClass) member).getApiDocPage();
				Assertions.assertTrue(entry1.getValue().isInstance(location));
				Assertions.assertFalse(location.toString().isEmpty());
			}
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
