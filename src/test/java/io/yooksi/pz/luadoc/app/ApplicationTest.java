package io.yooksi.pz.luadoc.app;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

	@Test
	void shouldReturnAllMatchingCommands() {

		Assertions.assertNotNull(Application.getMatchingCommand(new String[]{ "lua" }));
		Assertions.assertNotNull(Application.getMatchingCommand(new String[]{ "java" }));

		Assertions.assertNull(Application.getMatchingCommand(new String[]{}));
		Assertions.assertNull(Application.getMatchingCommand(new String[]{ "t" }));
		Assertions.assertNull(Application.getMatchingCommand(new String[]{ "t", "lua" }));
		Assertions.assertNull(Application.getMatchingCommand(new String[]{ "t", "java" }));
	}

	@Test
	void shouldRecognizeAllCommands() {

		Assertions.assertTrue(Application.isRecognizedCommand("lua"));
		Assertions.assertTrue(Application.isRecognizedCommand("java"));

		Assertions.assertFalse(Application.isRecognizedCommand(""));
		Assertions.assertFalse(Application.isRecognizedCommand("t"));
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingCommands() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> Application.getCommands().add(new Command("cmd", new Options())));
	}
}
