package io.yooksi.pz.luadoc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
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
	void shouldThrowExceptionWhenApplicationRunWithNoAppArg() {

		// No application argument supplied
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Main.main(new String[]{}));
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

		Assertions.assertThrows(NoSuchFileException.class,
				() -> Main.main(new String[]{ "-java", "invalid/path" }));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNoPathArg() {

		// IndexOutOfBoundsException
		Assertions.assertThrows(RuntimeException.class,
				() -> Main.main(new String[]{ "-lua" }));
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
		Assertions.assertEquals(EmmyLua.CLASS.create("sampleLua"), read.get(1));
	}

	@Test
	void whenApplicationRunShouldConvertJavaToLuaDoc() throws IOException {

		Main.main(new String[]{ "-java", file.getPath(), "src/test/resources/Pause.html" });
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
