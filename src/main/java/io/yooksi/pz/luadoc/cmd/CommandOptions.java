package io.yooksi.pz.luadoc.cmd;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public final class CommandOptions {

	static final Option INPUT_OPTION =
			Option.builder("i").longOpt("input-path").desc("input directory path")
					.type(File.class).required(true).hasArg().argName("path")
					.valueSeparator(' ').build();

	static final Option OUTPUT_OPTION =
			Option.builder("o").longOpt("output-path").desc("output directory path")
					.type(File.class).required(false).hasArg().argName("path")
					.valueSeparator(' ').build();

	static final Option API_OPTION =
			Option.builder("a").longOpt("api-docs").desc("read online api from url")
					.type(URL.class).required(false).hasArg().argName("url")
					.valueSeparator(' ').build();

	static final Options LUA_OPTIONS = new Options();
	static final Options JAVA_OPTIONS = new Options();

	static
	{
		LUA_OPTIONS.addOption(clone(INPUT_OPTION))
				.addOption(clone(OUTPUT_OPTION));

		OptionGroup javaOptGroup = createRequiredOptionGroup(
				clone(INPUT_OPTION), clone(API_OPTION)
		);
		JAVA_OPTIONS.addOptionGroup(javaOptGroup)
				.addOption(clone(OUTPUT_OPTION));
	}

	private static Option clone(Option option) {
		return (Option) option.clone();
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
