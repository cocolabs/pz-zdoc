package io.yooksi.pz.luadoc.app;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.doc.JavaDoc;

public class CommandLine extends org.apache.commons.cli.CommandLine {

	private static final CommandParser PARSER = new CommandParser();
	private static final HelpFormatter FORMATTER = new HelpFormatter();

	protected CommandLine(Option[] options, String[] args) {

		Arrays.stream(options).forEach(this::addOption);
		Arrays.stream(args).forEach(this::addArg);
	}

	protected CommandLine(org.apache.commons.cli.CommandLine cmdLine) {
		this(cmdLine.getOptions(), cmdLine.getArgs());
	}

	public static CommandLine parse(Options options, String[] args) throws ParseException {

		/* remove command arg from array so the parser doesnt treat
		 * it as an unrecognized element and doesnt parse other elements */
		String[] rawArgs = ArrayUtils.remove(args, 0);

		/* before parsing command try to find help option, note that
		 * it will fail if the help token is not the FIRST parsed token */
		CommandLine result = PARSER.parse(AppOptions.HELP_OPTIONS, rawArgs, true);

		/* if user doesnt want help parse command normally */
		return result.hasHelpOption() ? result : PARSER.parse(options, args);
	}

	public void printHelp(Command command, boolean withUsage) {
		FORMATTER.printHelp(command.getName(), command.getOptions(), withUsage);
	}

	public boolean isInputApi() {
		return hasOption(AppOptions.API_OPTION.getOpt());
	}

	public boolean hasHelpOption() {
		return hasOption(AppOptions.HELP_OPTION.getOpt());
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
