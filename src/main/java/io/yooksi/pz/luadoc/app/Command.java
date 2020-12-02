package io.yooksi.pz.luadoc.app;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Command {

	public static final Command LUA_COMMAND = new Command("lua", AppOptions.LUA_OPTIONS);
	public static final Command JAVA_COMMAND = new Command("java", AppOptions.JAVA_OPTIONS);

	private static final CommandLineParser PARSER = new DefaultParser();

	private final String name;
	private final Options options;

	protected Command(String name, Options options) {
		this.name = name;
		this.options = options;
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
