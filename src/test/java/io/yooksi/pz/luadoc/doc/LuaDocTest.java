package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.TestWorkspace;
import io.yooksi.pz.luadoc.lang.EmmyLua;

public class LuaDocTest extends TestWorkspace {

	LuaDocTest() {
		super("sampleLua.lua");
	}

	@Test
	void shouldCorrectlyDocumentSampleLuaFile() throws IOException {

		createSampleLuaFile();
		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(7, lines.size());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), lines.get(5));
	}

	@Test
	void shouldNotThrowExceptionWhenParsingTopLineLuaFile() throws IOException {

		String[] lines = { "sampleLua = luaClass:derive()" };
		FileUtils.writeLines(file, Arrays.asList(lines));

		// IndexOutOfBoundsException
		Assertions.assertDoesNotThrow(() ->
				LuaDoc.Parser.create(file).parse().writeToFile(file.toPath()));
	}

	@Test
	void shouldNotWriteToFileIfNoDocElementsFound() throws IOException {

		String expected = "--- No doc elements";
		FileUtils.writeLines(file, Collections.singletonList(expected));

		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		String actual = FileUtils.readLines(file, Charset.defaultCharset()).get(0);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	void shouldOverwriteExistingLuaAnnotation() throws IOException {

		String[] write = {
				"--- This is a sample comment",
				"---@class otherSampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));
		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(1));
	}

	@Test
	void shouldReadAnnotationsWithWhitespaces() throws IOException {

		String[] write = {
				"---  @class otherSampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));
		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(0));
	}

	@Test
	void shouldParseAnnotationIncludeParentType() throws IOException {

		String[] write = {
				"---@class sampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));
		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(0));

		write[1] = "sampleLua = luaClass:derive()";
		FileUtils.writeLines(file, Arrays.asList(write), false);
		LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());

		read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua", "luaClass" }), read.get(0));
	}
}
