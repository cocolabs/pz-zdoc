package io.yooksi.pz.zdoc.doc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.TestWorkspace;
import io.yooksi.pz.zdoc.lang.EmmyLua;

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

		String[] expected = new String[]{ "--- No doc elements" };
		String actual = writeParseAndReadLua(expected).get(0);

		Assertions.assertEquals(expected[0], actual);
	}

	@Test
	void shouldOverwriteExistingLuaAnnotation() throws IOException {

		List<String> read = writeParseAndReadLua(new String[]{
				"--- This is a sample comment",
				"---@class otherSampleLua",
				"sampleLua = luaClass:new()"
		});
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(1));
	}

	@Test
	void shouldReadAnnotationsWithWhitespaces() throws IOException {

		List<String> read = writeParseAndReadLua(new String[]{
				"---  @class otherSampleLua",
				"sampleLua = luaClass:new()"
		});
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(0));
	}

	@Test
	void shouldParseAnnotationIncludeParentType() throws IOException {

		String[] write = {
				"---@class sampleLua",
				"sampleLua = luaClass:new()"
		};
		List<String> read = writeParseAndReadLua(write);
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(0));

		write[1] = "sampleLua = luaClass:derive()";
		read = writeParseAndReadLua(write);
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua", "luaClass" }), read.get(0));
	}

	@Test
	void shouldNotCreateDuplicateClassAnnotations() {

		List<String> read = writeParseAndReadLua(new String[]{
				"sampleLua = {}",
				"",
				"local function testFun",
				"	sampleLua = {}",
				"end"
		});
		Assertions.assertEquals(1, read.stream()
				.filter(l -> l.contains("---@class")).count());
	}

	@Test
	void shouldRespectLineIndentationWhenCreatingAnnotation() {

		List<String> read = writeParseAndReadLua(new String[]{
				"local function testFun",
				"	sampleLua = {}",
				"	if condition then",
				"		otherLua = {}",
				"	end",
				"end"
		});
		Assertions.assertEquals("\t---@class sampleLua", read.get(1));
		Assertions.assertEquals("\t\t---@class otherLua", read.get(4));
	}

	private List<String> writeParseAndReadLua(String[] text) {

		try {
			FileUtils.writeLines(file, Arrays.asList(text));
			LuaDoc.Parser.create(file).parse().writeToFile(file.toPath());
			return FileUtils.readLines(file, Charset.defaultCharset());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
