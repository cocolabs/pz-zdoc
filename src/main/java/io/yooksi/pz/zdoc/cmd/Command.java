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
import java.util.stream.Collectors;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.Nullable;

public enum Command {

	HELP("help", "", new Options(), "print command usage info"),
	ANNOTATE("annotate", CommandOptions.LUA_OPTIONS, "annotate lua files with EmmyLua"),
	COMPILE("compile", CommandOptions.JAVA_OPTIONS, "compile javadoc to lua library");

	static final Command[] WORK_COMMANDS = Arrays.stream(Command.values())
			.filter(c -> c != Command.HELP).collect(Collectors.toSet()).toArray(new Command[]{});

	static
	{
		for (Command cmd : WORK_COMMANDS) {
			HELP.options.addOption(Option.builder(cmd.name).desc(cmd.help).build());
		}
	}

	final String name;
	final String prefix;
	final Options options;
	final String help;

	Command(String name, String prefix, Options options, String help) {

		this.name = name;
		this.prefix = prefix;
		this.options = options;
		this.help = help;
	}

	Command(String name, Options options, String help) {
		this(name, HelpFormatter.DEFAULT_OPT_PREFIX, options, help);
	}

	public static @Nullable Command get(String name) {

		for (Command value : Command.values())
		{
			if (value.name.equals(name)) {
				return value;
			}
		}
		return null;
	}

	public static @Nullable Command parse(String[] args) {
		return args.length > 0 ? get(args[0]) : null;
	}

	public static @Nullable Command parse(String[] args, int fromIndex) {
		return args.length > fromIndex ? get(args[fromIndex]) : null;
	}

	public String getName() {
		return name;
	}

	public Options getOptions() {
		return options;
	}
}
