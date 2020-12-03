package io.yooksi.pz.luadoc.app;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTest {

	private final Command[] commands = Command.values();

	@Test
	void shouldReturnAllMatchingCommands() {

		Assertions.assertNotNull(Command.parse(new String[]{ "lua" }));
		Assertions.assertNotNull(Command.parse(new String[]{ "java" }));

		Assertions.assertNull(Command.parse(new String[]{}));
		Assertions.assertNull(Command.parse(new String[]{ "t" }));
		Assertions.assertNull(Command.parse(new String[]{ "t", "lua" }));
		Assertions.assertNull(Command.parse(new String[]{ "t", "java" }));
	}

	@Test
	void shouldRecognizeAllCommands() {

		Assertions.assertNotNull(Command.get("lua"));
		Assertions.assertNotNull(Command.get("java"));

		Assertions.assertNull(Command.get(""));
		Assertions.assertNull(Command.get("t"));
	}

	@Test
	void shouldDetectAllCommandLineOptions() throws ParseException {

		String[][] argArrays = new String[][] {
				new String[] { "-in", "input/path", "-out", "output/path" },
				new String[] { "-out", "output/path", "-in", "input/path", }
		};
		for (Command command : commands)
		{
			for (String[] args : argArrays)
			{
				CommandLine cmd = CommandLine.parse(command.getOptions(),
						ArrayUtils.addFirst(args, command.getName()));

				command.getOptions().getOptions().forEach(opt -> Assertions.assertTrue(
						!opt.isRequired() || cmd.hasOption(opt.getOpt()))
				);
			}
		}
	}

	@Test
	void shouldThrowExceptionWhenMissingCommandArguments() {

		String[][] missingArgs = new String[][] {
//				new String[] { "-in", "input/path" },	 // missing output path
				new String[] { "-out", "output/path" }	// missing input path
		};
		for (String[] args : missingArgs) {
			Arrays.stream(commands).forEach(c -> Assertions.assertThrows(ParseException.class,
					() -> CommandLine.parse(c.getOptions(), ArrayUtils.addFirst(args, c.getName()))));
		}
		String[][] correctArgs = new String[][] {
				new String[] { "-in", "input/path", "-out", "output/path" },
				new String[] { "-out", "output/path", "-in", "input/path", }
		};
		for (String[] args : correctArgs) {
			Arrays.stream(commands).forEach(c -> Assertions.assertDoesNotThrow(() ->
					CommandLine.parse(c.getOptions(), ArrayUtils.addFirst(args, c.getName()))));
		}
	}

	@Test
	void shouldThrowExceptionWhenIncludingMutuallyExclusiveOptions() {

		String[] badArgs = new String[]{ "-in", "-a", "input/path", "-out", "output/path" };
		String[] goodArgs = new String[]{ "-a", "input/path", "-out", "output/path" };

		Assertions.assertThrows(ParseException.class, () -> CommandLine.parse(
				Command.JAVA.getOptions(), ArrayUtils.addFirst(badArgs, Command.JAVA.getName())));

		Assertions.assertDoesNotThrow(() -> CommandLine.parse(Command.JAVA.getOptions(),
				ArrayUtils.addFirst(goodArgs, Command.JAVA.getName())));
	}
}
