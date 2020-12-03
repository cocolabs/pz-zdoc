package io.yooksi.pz.luadoc.cmd;

import java.util.Properties;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandParser extends DefaultParser {

	@Override
	public CommandLine parse(Options options, String[] arguments) throws ParseException {
		return new CommandLine(super.parse(options, arguments));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException {
		return new CommandLine(super.parse(options, arguments, stopAtNonOption));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, Properties properties) throws ParseException {
		return new CommandLine(super.parse(options, arguments, properties));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, Properties properties,
							 boolean stopAtNonOption) throws ParseException {
		return new CommandLine(super.parse(options, arguments, properties, stopAtNonOption));
	}
}
