package io.yooksi.pz.luadoc.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.TestWorkspace;
import io.yooksi.pz.luadoc.lang.EmmyLua;

public class LuaDocParserTest extends TestWorkspace {

	LuaDocParserTest() {
		super("sampleLua.lua");
	}

	@Test
	void shouldThrowExceptionWhenDocumentingLuaWithNonExistingFiles() {

		File output = new File("output");
		Assertions.assertThrows(FileNotFoundException.class,
				() -> LuaDocParser.documentLuaFile(Paths.get("none"), file, output));

		Assertions.assertThrows(FileNotFoundException.class,
				() -> LuaDocParser.documentLuaFile(file.toPath(), new File("none"), output));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertTrue(outputDir.mkdir());
		Assertions.assertDoesNotThrow(() -> LuaDocParser.documentLuaFile(file, outputDir));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedNonExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertDoesNotThrow(() -> LuaDocParser.documentLuaFile(file, outputDir));
	}

	@Test
	void shouldCorrectlyDocumentSampleLuaFile() throws IOException {

		createSampleLuaFile();
		LuaDocParser.documentLuaFile(file);

		List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(7, lines.size());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), lines.get(5));
	}

	@Test
	void shouldKeepDirectoryHierarchyWhenDocumentingLuaFile() throws IOException {

		Path rootPath = dir.toPath();
		Path outputDir = rootPath.resolve("output");
		File sampleDir = rootPath.resolve("sample").toFile();
		Assertions.assertTrue(sampleDir.mkdir());

		createSampleLuaFile();
		FileUtils.moveFileToDirectory(file, sampleDir, false);
		File sampleFile = sampleDir.toPath().resolve(file.getName()).toFile();
		Assertions.assertTrue(sampleFile.exists());

		LuaDocParser.documentLuaFile(rootPath, sampleFile, outputDir);

		File outputFile = outputDir.resolve("sample").resolve(file.getName()).toFile();
		Assertions.assertTrue(outputFile.exists());

		List<String> lines = FileUtils.readLines(outputFile, Charset.defaultCharset());
		Assertions.assertEquals(7, lines.size());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), lines.get(5));
	}

	@Test
	void shouldNotThrowExceptionWhenParsingTopLineLuaFile() throws IOException {

		String[] lines = { "sampleLua = luaClass:derive()" };
		FileUtils.writeLines(file, Arrays.asList(lines));

		// IndexOutOfBoundsException
		Assertions.assertDoesNotThrow(() -> LuaDocParser.documentLuaFile(file));
	}

	@Test
	void shouldNotWriteToFileIfNoDocElementsFound() throws IOException {

		String expected = "--- No doc elements";
		FileUtils.writeLines(file, Collections.singletonList(expected));

		LuaDocParser.documentLuaFile(file);

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
		LuaDocParser.documentLuaFile(file);

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), read.get(1));
	}

	@Test
	void shouldReadAnnotationsWithWhitespaces() throws IOException {

		String[] write = {
				"---  @class otherSampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));
		LuaDocParser.documentLuaFile(file);

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), read.get(0));
	}

	@Test
	void shouldParseAnnotationIncludeParentType() throws IOException {

		String[] write = {
				"---@class sampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));
		LuaDocParser.documentLuaFile(file);

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), read.get(0));

		write[1] = "sampleLua = luaClass:derive()";
		FileUtils.writeLines(file, Arrays.asList(write), false);
		LuaDocParser.documentLuaFile(file);

		read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua", "luaClass"), read.get(0));
	}

	private void createSampleLuaFile() throws IOException {

		String[] lines = {
				"",
				"--*******************",
				"-- this is a comment",
				"--*******************",
				"",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(lines));
		Assertions.assertEquals(6, FileUtils.readLines(file, Charset.defaultCharset()).size());
	}
}
