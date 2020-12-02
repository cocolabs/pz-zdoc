package io.yooksi.pz.luadoc.app;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

public enum Command {

	LUA("lua", AppOptions.LUA_OPTIONS),
	JAVA("java", AppOptions.JAVA_OPTIONS);

	private static final CommandLineParser PARSER = new DefaultParser();

	private final String name;
	private final Options options;

	Command(String name, Options options) {
		this.name = name;
		this.options = options;
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

	public static @Nullable Command getFromArguments(String[] args) {
		return args.length > 0 ? get(args[0]) : null;
	}

	public CommandLine parse(String[] args) throws ParseException {
		return new CommandLine(PARSER.parse(options, args));
	}

	public String getName() {
		return name;
	}

	public Options getOptions() {
		return options;
	}
}
