package io.yooksi.pz.luadoc.cmd;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public final class CommandOptions {

	static final Option INPUT_OPTION =
			Option.builder("i").longOpt("input-path").desc("input directory path")
					.type(File.class).required(true).hasArg().valueSeparator(' ').build();

	static final Option OUTPUT_OPTION =
			Option.builder("o").longOpt("output-path").desc("output directory path")
					.type(File.class).required(false).hasArg().valueSeparator(' ').build();

	static final Option API_OPTION =
			Option.builder("a").longOpt("api-docs").desc("use online api docs")
					.type(URL.class).required(false).hasArg().valueSeparator(' ').build();

	static final Options LUA_OPTIONS = new Options();
	static final Options JAVA_OPTIONS = new Options();

	static
	{
		LUA_OPTIONS.addOption((Option) INPUT_OPTION.clone())
				.addOption((Option) OUTPUT_OPTION.clone());

		JAVA_OPTIONS.addOptionGroup(createRequiredOptionGroup(
				(Option) INPUT_OPTION.clone(), (Option) API_OPTION.clone())
		).addOption((Option) OUTPUT_OPTION.clone());
	}

	private static OptionGroup createRequiredOptionGroup(Option... options) {

		OptionGroup optionGroup = new OptionGroup();
		for (Option option : options) {
			optionGroup.addOption(option);
		}
		optionGroup.setRequired(true);
		return optionGroup;
	}
}
