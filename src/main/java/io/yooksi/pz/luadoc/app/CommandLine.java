package io.yooksi.pz.luadoc.app;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.doc.JavaDoc;

public class CommandLine extends org.apache.commons.cli.CommandLine {

	protected CommandLine(org.apache.commons.cli.CommandLine cmdLine) {

		Arrays.stream(cmdLine.getOptions()).forEach(this::addOption);
		Arrays.stream(cmdLine.getArgs()).forEach(this::addArg);
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
