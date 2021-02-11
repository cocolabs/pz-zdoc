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
package io.yooksi.pz.zdoc.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

/**
 * Apache Commons {@code CommandLine} wrapper providing additional methods.
 */
public class CommandLine extends org.apache.commons.cli.CommandLine {

	/** Used to parse application arguments to find command options. */
	private static final CommandParser PARSER = new CommandParser();

	CommandLine(org.apache.commons.cli.CommandLine cmdLine) {
		Arrays.stream(cmdLine.getOptions()).forEach(this::addOption);
		Arrays.stream(cmdLine.getArgs()).forEach(this::addArg);
	}

	public static CommandLine parse(Options options, String[] args) throws ParseException {
		return PARSER.parse(options, args);
	}

	/**
	 * Prints help text for the given {@code Command}.
	 *
	 * @see HelpFormatter#printHelp(String cmdLineSyntax, String header,
	 *        Options options, String footer, boolean autoUsage) HelpFormatter.printHelp(...)
	 */
	public static void printHelp(Command command) {

		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptPrefix(command.prefix);

		formatter.printHelp(command.name, command.help, command.options, "", true);
	}

	/**
	 * Prints help text for the given array of commands.
	 *
	 * @see HelpFormatter#printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header,
	 *        Options options, int leftPad, int descPad, String footer, boolean autoUsage)
	 * 		HelpFormatter.printHelp(...)
	 */
	public static void printHelp(Command[] commands) {

		//noinspection UseOfSystemOutOrSystemErr
		try (PrintWriter pw = new PrintWriter(System.out))
		{
			pw.println("See 'help <command>' to read about a specific command");
			for (Command command : commands)
			{
				HelpFormatter ft = new HelpFormatter();
				ft.setOptPrefix(command.prefix);

				pw.println();

				ft.printHelp(pw, ft.getWidth(), command.name, command.help, command.options,
						ft.getLeftPadding(), ft.getDescPadding(), "", true);
			}
			pw.flush();
		}
	}

	/**
	 * @return {@code Set} of class names specified in command options to exclude from
	 * 		compilation process or an empty list if exclude option has not been set.
	 *
	 * @see CommandOptions#EXCLUDE_CLASS_OPTION
	 */
	public Set<String> getExcludedClasses() {

		Option excludeOpt = CommandOptions.EXCLUDE_CLASS_OPTION;
		if (hasOption(excludeOpt.getOpt()))
		{
			String value = getParsedValue(excludeOpt);
			return Sets.newHashSet(value.split(","));
		}
		return new HashSet<>();
	}

	/**
	 * @return input path specified in command options.
	 */
	public Path getInputPath() {
		return ((File) getParsedValue(CommandOptions.INPUT_OPTION)).toPath();
	}

	/**
	 * @return output path specified in command options or {@code null} if
	 * 		output command option was not specified.
	 */
	public @Nullable Path getOutputPath() {

		File outputFile = getParsedValue(CommandOptions.OUTPUT_OPTION);
		return outputFile != null ? outputFile.toPath() : null;
	}

	@SuppressWarnings("unchecked")
	private <T> T getParsedValue(Option option) throws IllegalArgumentException {

		String sOption = option.getOpt();
		try {
			return ((T) getParsedOptionValue(sOption));
		}
		catch (ParseException | ClassCastException e) {
			throw new IllegalArgumentException(String.format("An error occurred while " +
					"parsing option value for option \"%s\"", getOptionValue(sOption)));
		}
	}
}
