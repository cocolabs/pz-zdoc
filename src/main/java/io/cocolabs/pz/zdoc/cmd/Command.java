/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc.cmd;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.Nullable;

/**
 * All available application commands.
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum Command {

	HELP("help", "", new Options(), "print command usage info"),
	VERSION("version", new Options(), "prints game installation version"),
	ANNOTATE("annotate", CommandOptions.LUA_OPTIONS, "annotate vanilla Lua with EmmyLua"),
	COMPILE("compile", CommandOptions.JAVA_OPTIONS, "compile Lua library from modding API");

	static
	{
		/* Add options (used only to print help) from static context to
		 * help command since enums are not fully instantiated yet
		 */
		Arrays.stream(Command.values()).filter(c -> c != Command.HELP).collect(Collectors.toSet())
				.forEach(c -> HELP.options.addOption(Option.builder(c.name).desc(c.help).build()));
	}

	/** Used to parse the command from application arguments. */
	final String name;

	/** Optional used to prefix printed commands in help context. */
	final String prefix;

	/** Possible options for this command. */
	final Options options;

	/** Command description printed with help command. */
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

	/** Returns command that matches the given name. */
	public static @Nullable Command get(String name) {

		for (Command value : Command.values())
		{
			if (value.name.equals(name)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Returns command that matches first array element.
	 *
	 * @return first array element or {@code null} if no matching command was found.
	 * @throws IllegalArgumentException if argument array is empty.
	 */
	public static @Nullable Command parse(String[] args) {

		if (args.length > 0) {
			return get(args[0]);
		}
		throw new IllegalArgumentException("Unable to parse command, argument array is empty.");
	}

	/**
	 * Returns command that matches first array element.
	 *
	 * @param fromIndex index to reference the first element from.
	 * @return first array element or {@code null} if no matching command was found.
	 *
	 * @throws IllegalArgumentException if argument array is empty.
	 * @see #parse(String[])
	 */
	public static @Nullable Command parse(String[] args, int fromIndex) {

		if (args.length > fromIndex) {
			return get(args[fromIndex]);
		}
		throw new IllegalArgumentException(String.format("Unable to parse command from array, " +
				"index %d is out of bounds for array size %d", fromIndex, args.length));
	}

	public String getName() {
		return name;
	}

	public Options getOptions() {
		return options;
	}
}
