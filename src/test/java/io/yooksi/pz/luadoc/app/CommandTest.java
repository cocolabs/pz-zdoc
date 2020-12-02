package io.yooksi.pz.luadoc.app;

import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTest {

	private final Set<Command> commands = Application.getCommands();

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
				CommandLine cmd = command.parse(args);
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
			commands.forEach(c -> Assertions.assertThrows(ParseException.class, () -> c.parse(args)));
		}
		String[][] correctArgs = new String[][] {
				new String[] { "-in", "input/path", "-out", "output/path" },
				new String[] { "-out", "output/path", "-in", "input/path", }
		};
		for (String[] args : correctArgs) {
			commands.forEach(c -> Assertions.assertDoesNotThrow(() -> c.parse(args)));
		}
	}

	@Test
	void shouldThrowExceptionWhenIncludingMutuallyExclusiveOptions() {

		String[] badArgs = new String[]{ "-in", "-a", "input/path", "-out", "output/path" };
		String[] goodArgs = new String[]{ "-a", "input/path", "-out", "output/path" };

		Assertions.assertThrows(ParseException.class, () -> Command.JAVA_COMMAND.parse(badArgs));
		Assertions.assertDoesNotThrow(() -> Command.JAVA_COMMAND.parse(goodArgs));
	}
}
