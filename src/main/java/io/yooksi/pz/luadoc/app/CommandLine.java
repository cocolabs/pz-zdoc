package io.yooksi.pz.luadoc.app;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

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
		return hasOption(AppOptions.API_OPTION.getOpt());
	}

	public @Nullable URL getInputUrl() {

		if (isInputApi())
		{
			Option option = AppOptions.API_OPTION;
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
		return ((File) getParsedValue(AppOptions.INPUT_OPTION)).toPath();
	}

	public @Nullable Path getOutputPath() {

		File outputFile = getParsedValue(AppOptions.OUTPUT_OPTION);
		return outputFile != null ? outputFile.toPath() : null;
	}

	@SuppressWarnings("unchecked")
	private <T> T getParsedValue(Option option) throws IllegalArgumentException {

		String sOption = option.getOpt();
		try {
			return ((T) getParsedOptionValue(sOption));
		}
		catch (ParseException | ClassCastException e) {
			throw new IllegalArgumentException('\"' + getOptionValue(sOption) + "\" is not a valid Path");
		}
	}
}
