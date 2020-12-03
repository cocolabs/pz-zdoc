package io.yooksi.pz.luadoc.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.doc.JavaDoc;

public class CommandLine extends org.apache.commons.cli.CommandLine {

	private static final CommandParser PARSER = new CommandParser();

	protected CommandLine(Option[] options, String[] args) {

		Arrays.stream(options).forEach(this::addOption);
		Arrays.stream(args).forEach(this::addArg);
	}

	protected CommandLine(org.apache.commons.cli.CommandLine cmdLine) {
		this(cmdLine.getOptions(), cmdLine.getArgs());
	}

	public static CommandLine parse(Options options, String[] args) throws ParseException {
		return PARSER.parse(options, args);
	}

	public static void printHelp(Command command) {

		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptPrefix(command.prefix);

		formatter.printHelp(command.name, command.help, command.options, "", true);
	}

	public static void printHelp(Command[] commands) {

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

	public boolean isInputApi() {
		return hasOption(CommandOptions.API_OPTION.getOpt());
	}

	public List<String> getExcludedClasses() {

		Option excludeOpt = CommandOptions.EXCLUDE_CLASS_OPTION;
		if (hasOption(excludeOpt.getOpt()))
		{
			String value = getParsedValue(excludeOpt);
			return Arrays.asList(value.split(","));
		}
		return new ArrayList<>();
	}

	public @Nullable URL getInputUrl() {

		if (isInputApi())
		{
			Option option = CommandOptions.API_OPTION;
			try {
				return getParsedValue(option);
			}
			catch (IllegalArgumentException e) {
				return JavaDoc.resolveApiURL(getOptionValue(option.getOpt()));
			}
		}
		return null;
	}

	public Path getInputPath() {
		return ((File) getParsedValue(CommandOptions.INPUT_OPTION)).toPath();
	}

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
