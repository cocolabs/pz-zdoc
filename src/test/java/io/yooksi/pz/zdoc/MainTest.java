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
package io.yooksi.pz.zdoc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import io.yooksi.pz.zdoc.cmd.Command;
import io.yooksi.pz.zdoc.lang.EmmyLua;

public class MainTest extends TestWorkspace implements IntegrationTest {

	MainTest() {
		super("sampleLua.lua");
	}

	private static Executable runMain(@Nullable Command command, String input, String output) {
		return () -> Main.main(formatAppArgs(command != null ? command.getName() : "", input, output));
	}

	@TestOnly
	public static String[] formatAppArgs(String command, String input, String output) {

		List<String> args = new java.util.ArrayList<>();
		args.add(command);
		if (!input.isEmpty())
		{
			args.add("-i");
			args.add(input);
		}
		if (!output.isEmpty())
		{
			args.add("-o");
			args.add(output);
		}
		return args.toArray(new String[]{});
	}

	@TestOnly
	public static String[] formatAppArgs(Command command, String input, String output) {
		return formatAppArgs(command.getName(), input, output);
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithMissingCommand() {

		// Missing or unknown command argument
		Assertions.assertThrows(ParseException.class,
				runMain(null, "input/path", "output/path"));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithMissingArgs() {

		Arrays.stream(Command.values()).filter(c -> c != Command.HELP)
				.forEach(c -> Assertions.assertThrows(ParseException.class,
						runMain(c, "", "output/path")));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNonDirectoryOutput() throws IOException {

		File notDirFile = dir.toPath().resolve("not_dir.file").toFile();
		Assertions.assertTrue(notDirFile.createNewFile());

		Assertions.assertThrows(IllegalArgumentException.class,
				runMain(Command.COMPILE, "input/path", notDirFile.getPath()));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertTrue(outputDir.mkdir());
		Assertions.assertDoesNotThrow(runMain(Command.ANNOTATE, file.getPath(), outputDir.getPath()));
	}

	@Test
	void shouldDocumentLuaFileWithSpecifiedNonExistingOutputDir() throws IOException {

		createSampleLuaFile();
		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertDoesNotThrow(runMain(Command.ANNOTATE, file.getPath(), outputDir.getPath()));
	}

	@Test
	void whenApplicationRunShouldDocumentLuaClasses() throws Throwable {

		String[] write = {
				"--- This is a sample comment",
				"---@class otherSampleLua",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(write));

		runMain(Command.ANNOTATE, dir.getPath(), "").execute();

		List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), read.get(1));
	}

	@Test
	void shouldKeepDirectoryHierarchyWhenDocumentingLuaFile() throws Throwable {

		Path rootPath = dir.toPath();
		Path outputDir = rootPath.resolve("output");
		File sampleDir = rootPath.resolve("sample").toFile();
		Assertions.assertTrue(sampleDir.mkdir());

		createSampleLuaFile();
		FileUtils.moveFileToDirectory(file, sampleDir, false);
		File sampleFile = sampleDir.toPath().resolve(file.getName()).toFile();
		Assertions.assertTrue(sampleFile.exists());

		runMain(Command.ANNOTATE, rootPath.toString(), outputDir.toString()).execute();

		File outputFile = outputDir.resolve("sample").resolve(file.getName()).toFile();
		Assertions.assertTrue(outputFile.exists());

		List<String> lines = FileUtils.readLines(outputFile, Charset.defaultCharset());
		Assertions.assertEquals(7, lines.size());
		Assertions.assertEquals(EmmyLua.CLASS.create(new String[]{ "sampleLua" }), lines.get(5));
	}

	@Test
	void whenApplicationRunShouldConvertJavaToLuaDoc() throws Throwable {

		File outputDir = dir.toPath().resolve("output").toFile();
		Assertions.assertTrue(outputDir.mkdir());

		String input = "src/test/resources/Sample.html";
		runMain(Command.COMPILE, input, outputDir.getPath()).execute();

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

	@Test
	void shouldGetExposedJavaClasses() throws IllegalAccessException {
		/*
		 * just confirm that the list is populated,
		 * validating exposed objects would be too much setup
		 */
		Assertions.assertFalse(Main.getExposedJava(true).isEmpty());
	}
}
