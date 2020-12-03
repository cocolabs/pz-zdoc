package io.yooksi.pz.luadoc.app;

import org.apache.commons.cli.Options;
import org.jetbrains.annotations.Nullable;

public enum Command {

	LUA("lua", AppOptions.LUA_OPTIONS),
	JAVA("java", AppOptions.JAVA_OPTIONS);

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

	public static @Nullable Command parse(String[] args) {
		return args.length > 0 ? get(args[0]) : null;
	}

	public String getName() {
		return name;
	}

	public Options getOptions() {
		return options;
	}
}
