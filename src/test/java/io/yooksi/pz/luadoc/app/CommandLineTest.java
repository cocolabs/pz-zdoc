package io.yooksi.pz.luadoc.app;

import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.luadoc.doc.JavaDoc;

public class CommandLineTest {

	private final Command[] commands = Command.values();

	@Test
	void shouldProperlyParseCommandInputPath() throws ParseException {

		String path = Paths.get("input/path").toString();
		for (Command command : commands)
		{
			String[] args = new String[] { "-in", path, "-out", "output/path" };
			CommandLine cmdLIne = command.parse(args);

			Assertions.assertEquals(path, cmdLIne.getInputPath().toString());
		}
	}

	@Test
	void shouldProperlyParseCommandOutputPath() throws ParseException {

		String path = Paths.get("output/path").toString();
		for (Command command : commands)
		{
			String[] args = new String[] { "-in", "input/path", "-out", path };
			CommandLine cmdLIne = command.parse(args);

			Object outputPath = Objects.requireNonNull(cmdLIne.getOutputPath());
			Assertions.assertEquals(path, outputPath.toString());
		}
	}

	@Test
	void shouldThrowExceptionWhenParsingMalformedCommandPath() {

		String path = "t*st/pa!h";
		for (Command command : commands)
		{
			Assertions.assertThrows(InvalidPathException.class, () -> command.parse(
					new String[] { "-in", path, "-out", "output/path" }).getInputPath());

			Assertions.assertThrows(InvalidPathException.class, () -> command.parse(
					new String[] { "-in", "input/path", "-out", path }).getOutputPath());
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

			String[] args = new String[]{ "--api", target, "-out", "output/path",  };
			CommandLine cmdLIne = Command.JAVA.parse(args);

			Assertions.assertEquals(expected, cmdLIne.getInputUrl());
		}
	}
}
