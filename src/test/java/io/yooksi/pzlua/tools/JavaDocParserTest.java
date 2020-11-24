package io.yooksi.pzlua.tools;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.yooksi.pzlua.tools.method.LuaFunction;
import io.yooksi.pzlua.tools.method.Method;
import io.yooksi.pzlua.tools.method.Parameter;
import io.yooksi.pzlua.tools.parse.JavaDocParser;

public class JavaDocParserTest extends TestWorkspace {

	private static JavaDocParser PauseJavaDocParser;
	private static JavaDocParser PcxJavaDocParser;

	JavaDocParserTest() {
		super("outputLua.lua");
	}

	@BeforeAll
	static void initializeStaticParsers() throws IOException {

		PcxJavaDocParser = JavaDocParser.loadFile("src/test/resources/Pcx.html");
		PauseJavaDocParser = JavaDocParser.loadFile("src/test/resources/Pause.html");
	}

	@Test
	void shouldCorrectlyParseSingleJavaMethod() {

		JavaDocParser parser = PcxJavaDocParser;
		List<Method> methods = parser.parseMethods(JavaDocParser.JAVA_METHOD_PARSER);

		Assertions.assertEquals(1, methods.size());
		Assertions.assertEquals("java.awt.Image getImage()",methods.get(0).toString());
	}

	@Test
	void shouldCorrectlyParseMultipleJavaMethod() {

		JavaDocParser parser = PauseJavaDocParser;
		List<Method> methods = parser.parseMethods(JavaDocParser.JAVA_METHOD_PARSER);

		String[] methodName = {
				"begin", "DoesInstantly", "init", "IsFinished", "update"
		};
		Assertions.assertEquals(5, methods.size());
		for (int i = 0; i < methods.size(); i++) {
			Assertions.assertEquals(methodName[i], methods.get(i).getName());
		}
	}

	@Test
	void shouldCorrectlyParseEmptyMethodParams() {

		JavaDocParser parser = PcxJavaDocParser;
		List<Method> methods = parser.parseMethods(JavaDocParser.JAVA_METHOD_PARSER);

		Parameter[] params = methods.get(0).getParams();
		Assertions.assertEquals(0, params.length);
	}

	@Test
	void shouldCorrectlyParseSingleLuaMethod() {

		JavaDocParser parser = PcxJavaDocParser;
		List<Method> methods = parser.parseMethods(JavaDocParser.LUA_METHOD_PARSER);

		Assertions.assertEquals(1, methods.size());
		Assertions.assertEquals("function getImage()",methods.get(0).toString());
	}

	@Test
	void shouldGenerateValidLuaMethodDocumentation() {

		JavaDocParser parser = PauseJavaDocParser;
		List<Method> methods = parser.parseMethods(JavaDocParser.LUA_METHOD_PARSER);
		List<String> luaDoc = ((LuaFunction) methods.get(2)).generateLuaDoc();

		String[] expectedDoc = {
				"---@param object String",
				"---@param params String[]",
				"---@return void"
		};
		for (int i = 0; i < luaDoc.size(); i++) {
			Assertions.assertEquals(expectedDoc[i], luaDoc.get(i));
		}
	}

	@Test
	void shouldCorrectlyConvertJavaToLuaDocumentation() throws IOException {

		JavaDocParser parser = PauseJavaDocParser;
		parser.convertJavaToLuaDoc(file.toPath());

		List<String> output = FileUtils.readLines(file, Charset.defaultCharset());
		String[] actual = output.toArray(new String[] {});

		String[] expected = {
				"---@return void",
				"function begin()",
				"",
				"---@return boolean",
				"function DoesInstantly()",
				"",
				"---@param object String",
				"---@param params String[]",
				"---@return void",
				"function init(object, params)",
				"",
				"---@return boolean",
				"function IsFinished()",
				"",
				"---@return void",
				"function update()",
		};
		for (int i = 0; i < expected.length; i++) {
			Assertions.assertEquals(expected[i], actual[i]);
		}
	}
}
