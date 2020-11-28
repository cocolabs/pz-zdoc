package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.TestWorkspace;
import io.yooksi.pz.luadoc.element.*;

public class JavaDocTest extends TestWorkspace {

	private static final Logger logger = LogManager.getLogger(JavaDocTest.class);

	private static JavaDoc.WebParser globalJavaDocParser;
	private static JavaDoc.FileParser pauseJavaDocParser;
	private static JavaDoc.FileParser pcxJavaDocParser;

	JavaDocTest() {
		super("outputLua.lua");
	}

	@BeforeAll
	static void initializeStaticParsers() throws IOException {

		pcxJavaDocParser = JavaDoc.FileParser.create("src/test/resources/Pcx.html");
		pauseJavaDocParser = JavaDoc.FileParser.create("src/test/resources/Pause.html");
	}

	@Test
	void shouldCorrectlyParseSingleJavaMethod() {

		List<JavaMethod> methods = pcxJavaDocParser.parse().getMethods();

		Assertions.assertEquals(1, methods.size());
		Assertions.assertEquals("java.awt.Image getImage()", methods.get(0).toString());
	}

	@Test
	void shouldCorrectlyParseMultipleJavaMethod() {

		List<JavaMethod> methods = pauseJavaDocParser.parse().getMethods();
		Assertions.assertEquals(5, methods.size());

		String[] methodName = {
				"begin", "DoesInstantly", "init", "IsFinished", "update"
		};
		for (int i = 0; i < methods.size(); i++) {
			Assertions.assertEquals(methodName[i], methods.get(i).getName());
		}
	}

	@Test
	void shouldCorrectlyParseEmptyMethodParams() {

		List<JavaMethod> methods = pcxJavaDocParser.parse().getMethods();

		Parameter[] params = methods.get(0).getParams();
		Assertions.assertEquals(0, params.length);
	}

	@Test
	void shouldCorrectlyParseSingleLuaMethod() {

		List<LuaMethod> methods = pcxJavaDocParser.parse().convertToLuaDoc(false).getMethods();

		Assertions.assertEquals(1, methods.size());
		Assertions.assertEquals("function getImage()", methods.get(0).toString());
	}

	@Test
	void shouldGenerateValidLuaMethodDocumentation() {

		List<JavaMethod> methods = pauseJavaDocParser.parse().getMethods();
		List<String> luaDoc = LuaMethod.Parser.create(methods.get(2)).parse().annotate();

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

		pauseJavaDocParser.parse().convertToLuaDoc(true).writeToFile(file.toPath());

		List<String> output = FileUtils.readLines(file, Charset.defaultCharset());
		String[] actual = output.toArray(new String[]{});

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

	@Test
	void shouldCorrectlyParseJavaDocMemberClasses() throws IOException {

		Map<JavaDoc.Parser<?>, Class<?>> dataMap = new HashMap<>();
		dataMap.put(JavaDoc.WebParser.create(JavaDoc.PZ_API_GLOBAL_URL), URL.class);
		dataMap.put(JavaDoc.FileParser.create("src/test/resources/GlobalObject.html"), Path.class);

		for (Map.Entry<JavaDoc.Parser<?>, Class<?>> entry1 : dataMap.entrySet())
		{
			Map<String, ? extends MemberClass> map = entry1.getKey().parse().getMembers();
			for (Map.Entry<String, ? extends MemberClass> entry2 : map.entrySet())
			{
				Assertions.assertFalse(entry2.getKey().isEmpty());

				MemberClass member = entry2.getValue();
				Assertions.assertTrue(member instanceof JavaClass);

				Object location = ((JavaClass<?>)member).getLocation();
				Assertions.assertTrue(entry1.getValue().isInstance(location));
				Assertions.assertFalse(location.toString().isEmpty());
			}
		}
	}
}
