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

import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTest {

	@Test
	void shouldReturnAllMatchingCommands() {

		Assertions.assertNotNull(Command.parse(new String[]{ "lua" }));
		Assertions.assertNotNull(Command.parse(new String[]{ "java" }));

		Assertions.assertNull(Command.parse(new String[]{}));
		Assertions.assertNull(Command.parse(new String[]{ "t" }));
		Assertions.assertNull(Command.parse(new String[]{ "t", "lua" }));
		Assertions.assertNull(Command.parse(new String[]{ "t", "java" }));
	}

	@Test
	void shouldRecognizeAllCommands() {

		Assertions.assertNotNull(Command.get("lua"));
		Assertions.assertNotNull(Command.get("java"));

		Assertions.assertNull(Command.get(""));
		Assertions.assertNull(Command.get("t"));
	}

	@Test
	void shouldDetectAllCommandLineOptions() throws ParseException {

		String[][] argArrays = new String[][]{
				new String[]{ "-i", "input/path", "-o", "output/path" },
				new String[]{ "-o", "output/path", "-i", "input/path", }
		};
		for (Command command : Command.WORK_COMMANDS)
		{
			for (String[] args : argArrays)
			{
				CommandLine cmd = CommandLine.parse(command.options,
						ArrayUtils.addFirst(args, command.name));

				command.options.getOptions().forEach(opt -> Assertions.assertTrue(
						!opt.isRequired() || cmd.hasOption(opt.getOpt()))
				);
			}
		}
	}

	@Test
	void shouldThrowExceptionWhenMissingCommandArguments() {

		String[][] missingArgs = new String[][]{
//				new String[] { "-i", "input/path" },	 // missing output path
				new String[]{ "-o", "output/path" }    // missing input path
		};
		for (String[] args : missingArgs) {
			Arrays.stream(Command.WORK_COMMANDS).forEach(c -> Assertions.assertThrows(ParseException.class,
					() -> CommandLine.parse(c.options, ArrayUtils.addFirst(args, c.name))));
		}
		String[][] correctArgs = new String[][]{
				new String[]{ "-i", "input/path", "-o", "output/path" },
				new String[]{ "-o", "output/path", "-i", "input/path", }
		};
		for (String[] args : correctArgs)
		{
			Arrays.stream(Command.WORK_COMMANDS).forEach(c -> Assertions.assertDoesNotThrow(() ->
					CommandLine.parse(c.options, ArrayUtils.addFirst(args, c.name))));
		}
	}

	@Test
	void shouldThrowExceptionWhenIncludingMutuallyExclusiveOptions() {

		String[] badArgs = new String[]{ "-i", "-a", "input/path", "-o", "output/path" };
		String[] goodArgs = new String[]{ "-a", "input/path", "-o", "output/path" };

		Assertions.assertThrows(ParseException.class, () -> CommandLine.parse(
				Command.JAVA.options, ArrayUtils.addFirst(badArgs, Command.JAVA.name)));

		Assertions.assertDoesNotThrow(() -> CommandLine.parse(Command.JAVA.options,
				ArrayUtils.addFirst(goodArgs, Command.JAVA.name)));
	}
}
