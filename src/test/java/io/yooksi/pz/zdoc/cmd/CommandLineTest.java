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

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.MainTest;
import io.yooksi.pz.zdoc.doc.JavaDoc;

public class CommandLineTest {

	@Test
	void shouldProperlyParseCommandInputPath() throws ParseException {

		String path = Paths.get("input/path").toString();
		for (Command command : Command.WORK_COMMANDS)
		{
			String[] args = MainTest.formatAppArgs(command, path, "output/path");
			CommandLine cmdLIne = CommandLine.parse(command.options, args);

			Assertions.assertEquals(path, cmdLIne.getInputPath().toString());
		}
	}

	@Test
	void shouldProperlyParseCommandOutputPath() throws ParseException {

		String path = Paths.get("output/path").toString();
		for (Command command : Command.WORK_COMMANDS)
		{
			String[] args = MainTest.formatAppArgs(command, "input/path", path);
			CommandLine cmdLIne = CommandLine.parse(command.options, args);

			Object outputPath = Objects.requireNonNull(cmdLIne.getOutputPath());
			Assertions.assertEquals(path, outputPath.toString());
		}
	}

	@Test
	void shouldThrowExceptionWhenParsingMalformedCommandPath() {

		String path = '\u0000' + "/p*!h";
		for (Command command : Command.WORK_COMMANDS)
		{
			final String[] args1 = MainTest.formatAppArgs(command, path, "output/path");
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.options, args1).getInputPath());

			final String[] args2 = MainTest.formatAppArgs(command, "input/path", path);
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.options, args2).getOutputPath());
		}
	}

	@Test
	void shouldProperlyParseCommandInputURL() throws ParseException {

		String[] targets = {
				"zombie/inventory/InventoryItem.html",
				JavaDoc.resolveApiURL("zombie/inventory/InventoryItem.html").toString()
		};
		for (String target : targets)
		{
			URL expected = JavaDoc.resolveApiURL(target);

			String[] args = new String[]{ Command.COMPILE.name, "-a", target, "-o", "output/path" };
			CommandLine cmdLIne = CommandLine.parse(Command.COMPILE.options, args);

			Assertions.assertEquals(expected, cmdLIne.getInputUrl());
		}
	}
}
