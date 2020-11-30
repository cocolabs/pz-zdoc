package io.yooksi.pz.luadoc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.lang.EmmyLua;

public class MainTest extends TestWorkspace {

	MainTest() {
		super("sampleLua.lua");
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithMissingAppArgs() {

		Arrays.stream(new String[][]{
						// No application argument supplied
						new String[]{},

						// No file path supplied
						new String[]{ "-lua" },

						// Java doc location not specified
						new String[]{ "-java" },

						// No output file path supplied
						new String[]{ "-java", "location" }
				}
		).forEach(a -> Assertions.assertThrows(
				IllegalArgumentException.class, () -> Main.main(a)));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithInvalidAppArg() {

		// Malformed/unknown application argument
		String[] invalidArg = { "--lua", "lua", "--java", "java" };
		for (String arg : invalidArg) {
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Main.main(new String[]{ arg }));
		}
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithMalformedPathArg() {

		// Invalid file path or URL
		String[][] invalidPathArgs = {
				new String[]{ "-lua", "C:/fi*e" },
				new String[]{ "-java", "C:/fi*e" },
		};
		for (String[] args : invalidPathArgs) {
			Assertions.assertThrows(IllegalArgumentException.class, () -> Main.main(args));
		}
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithInvalidPathArg() {

		Assertions.assertThrows(NoSuchFileException.class,
				() -> Main.main(new String[]{ "-lua", "invalid/path" }));

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Main.main(new String[]{ "-java", "invalid/path.text" }));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNoPathArg() {

		// IndexOutOfBoundsException
		Assertions.assertThrows(RuntimeException.class,
				() -> Main.main(new String[]{ "-lua" }));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNonDirectoryOutput() throws IOException {

		File notDirFile = dir.toPath().resolve("not_dir.file").toFile();
		Assertions.assertTrue(notDirFile.createNewFile());

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Main.main(new String[]{ "-java", "--api", notDirFile.getPath() }));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertTrue(outputDir.mkdir());
		Assertions.assertDoesNotThrow(() -> Main.main(new String[]{
				"-lua", file.getPath(), outputDir.getPath() }));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedNonExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertDoesNotThrow(() -> Main.main(new String[]{
				"-lua", file.getPath(), outputDir.getPath() }));
	}

	@Test
	void whenApplicationRunShouldDocumentLuaClasses() throws IOException {

		String[] write = {
				"--- This is a sample comment",
				"---@class otherSampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));

		Main.main(new String[]{ "-lua", dir.getPath() });

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(1));
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

		Main.main(new String[]{ "-lua", rootPath.toString(), outputDir.toString() });

		File outputFile = outputDir.resolve("sample").resolve(file.getName()).toFile();
		Assertions.assertTrue(outputFile.exists());

		List<String> lines = FileUtils.readLines(outputFile, Charset.defaultCharset());
		Assertions.assertEquals(7, lines.size());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), lines.get(5));
	}

	@Test
	void whenApplicationRunShouldConvertJavaToLuaDoc() throws IOException {

		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertTrue(outputDir.mkdir());

		Main.main(new String[]{ "-java", "src/test/resources/Sample.html", outputDir.getPath() });
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
		List<String> actual = FileUtils.readLines(file, Charset.defaultCharset());
		for (int i = 0; i < actual.size(); i++) {
			Assertions.assertEquals(expected[i], actual.get(i));
		}
	}
}
