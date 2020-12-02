package io.yooksi.pz.luadoc.app;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public final class AppOptions {

	static final Option INPUT_OPTION =
			Option.builder("in").desc("input directory path")
					.type(File.class).required(true).hasArg().valueSeparator(' ').build();

	static final Option OUTPUT_OPTION =
			Option.builder("out").desc("output directory path")
					.type(File.class).required(false).hasArg().valueSeparator(' ').build();

	static final Option API_OPTION =
			Option.builder("a").longOpt("api").desc("use online api docs")
					.type(URL.class).required(false).hasArg().valueSeparator(' ').build();

	static final Options LUA_OPTIONS = new Options();
	static final Options JAVA_OPTIONS = new Options();

	static
	{
		LUA_OPTIONS.addOption((Option) INPUT_OPTION.clone())
				.addOption((Option) OUTPUT_OPTION.clone());

		OptionGroup javaInputOptions = new OptionGroup()
				.addOption((Option) INPUT_OPTION.clone())
				.addOption((Option) API_OPTION.clone());

		javaInputOptions.setRequired(true);

		JAVA_OPTIONS.addOptionGroup(javaInputOptions)
				.addOption((Option) OUTPUT_OPTION.clone());
	}
}
