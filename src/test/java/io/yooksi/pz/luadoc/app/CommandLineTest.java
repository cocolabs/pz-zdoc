package io.yooksi.pz.luadoc.app;

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.MainTest;
import io.yooksi.pz.luadoc.doc.JavaDoc;

public class CommandLineTest {

	private final Command[] commands = Command.values();

	@Test
	void shouldProperlyParseCommandInputPath() throws ParseException {

		String path = Paths.get("input/path").toString();
		for (Command command : commands)
		{
			String[] args = MainTest.formatAppArgs(command, path, "output/path");
			CommandLine cmdLIne = CommandLine.parse(command.getOptions(), args);

			Assertions.assertEquals(path, cmdLIne.getInputPath().toString());
		}
	}

	@Test
	void shouldProperlyParseCommandOutputPath() throws ParseException {

		String path = Paths.get("output/path").toString();
		for (Command command : commands)
		{
			String[] args = MainTest.formatAppArgs(command, "input/path", path);
			CommandLine cmdLIne = CommandLine.parse(command.getOptions(), args);

			Object outputPath = Objects.requireNonNull(cmdLIne.getOutputPath());
			Assertions.assertEquals(path, outputPath.toString());
		}
	}

	@Test
	void shouldThrowExceptionWhenParsingMalformedCommandPath() {

		String path = "t*st/pa!h";
		for (Command command : commands)
		{
			final String[] args1 = MainTest.formatAppArgs(command, path, "output/path");
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.getOptions(), args1).getInputPath());

			final String[] args2 = MainTest.formatAppArgs(command, "input/path", path);
			Assertions.assertThrows(InvalidPathException.class, () ->
					CommandLine.parse(command.getOptions(), args2).getOutputPath());
		}
	}

	@Test
	void shouldProperlyParseCommandInputURL() throws ParseException {

		String[] targets = {
				"zombie/inventory/InventoryItem.html",
				JavaDoc.resolveApiURL("zombie/inventory/InventoryItem.html").toString()
		};
		for (String target : targets)
		{
			URL expected = JavaDoc.resolveApiURL(target);

			String[] args = new String[]{ Command.JAVA.getName(), "--api", target, "-out", "output/path" };
			CommandLine cmdLIne = CommandLine.parse(Command.JAVA.getOptions(), args);

			Assertions.assertEquals(expected, cmdLIne.getInputUrl());
		}
	}
}
