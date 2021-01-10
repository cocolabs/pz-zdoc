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
package io.yooksi.pz.zdoc.cmd;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandLineTest {

	private static final Command[] COMMANDS = Arrays.stream(Command.values())
			.filter(c -> c != Command.HELP).collect(Collectors.toSet()).toArray(new Command[]{});

	@Test
	void shouldProperlyParseCommandInputPath() throws ParseException {

		String path = Paths.get("input/path").toString();
		for (Command command : COMMANDS)
		{
			String[] args = formatAppArgs(command, path, "output/path");
			CommandLine cmdLIne = CommandLine.parse(command.options, args);

			Assertions.assertEquals(path, cmdLIne.getInputPath().toString());
		}
	}

	@Test
	void shouldProperlyParseCommandOutputPath() throws ParseException {

		String path = Paths.get("output/path").toString();
		for (Command command : COMMANDS)
		{
			String[] args = formatAppArgs(command, "input/path", path);
			CommandLine cmdLIne = CommandLine.parse(command.options, args);

			Object outputPath = Objects.requireNonNull(cmdLIne.getOutputPath());
			Assertions.assertEquals(path, outputPath.toString());
		}
	}

	@Test
	void shouldThrowExceptionWhenParsingMalformedCommandPath() {

		String path = '\u0000' + "/p*!h";
		for (Command command : COMMANDS)
		{
			final String[] args1 = formatAppArgs(command, path, "output/path");
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.options, args1).getInputPath());

			final String[] args2 = formatAppArgs(command, "input/path", path);
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.options, args2).getOutputPath());
		}
	}

	@TestOnly
	private static String[] formatAppArgs(String command, String input, String output) {

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
	private static String[] formatAppArgs(Command command, String input, String output) {
		return formatAppArgs(command.getName(), input, output);
	}
}
