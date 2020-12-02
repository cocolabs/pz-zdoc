package io.yooksi.pz.luadoc.app;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

public class Application {

	private static final Set<Command> COMMANDS = new java.util.HashSet<>();

	static
	{
		COMMANDS.add(Command.LUA_COMMAND);
		COMMANDS.add(Command.JAVA_COMMAND);
	}

	public static Set<Command> getCommands() {
		return Collections.unmodifiableSet(COMMANDS);
	}

	public static boolean isRecognizedCommand(String name) {
		return COMMANDS.stream().anyMatch(c -> c.getName().equals(name));
	}

	public static @Nullable Command getCommand(String name) {
		return COMMANDS.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	public static @Nullable Command getMatchingCommand(String[] args) {
		return args.length > 0 ? getCommand(args[0]) : null;
	}
}
