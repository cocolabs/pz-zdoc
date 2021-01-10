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

import io.yooksi.pz.zdoc.cmd.Command;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Tag;

import java.util.List;

@Tag("integration")
public interface IntegrationTest {

	@TestOnly
	static String[] formatAppArgs(String command, String input, String output) {

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
	static String[] formatAppArgs(Command command, String input, String output) {
		return formatAppArgs(command.getName(), input, output);
	}
}
