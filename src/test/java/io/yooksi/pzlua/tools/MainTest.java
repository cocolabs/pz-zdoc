package io.yooksi.pzlua.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest extends TestWorkspace {

	MainTest() {
		super("sampleLua.lua");
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNoAppArg() {

		// No application argument supplied
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Main.main(new String[]{}));
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithInvalidAppArg() {

		// Malformed/unknown application argument
		String[] invalidArg = { "--lua", "lua", "--java", "java" };
		for (String arg : invalidArg) {
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Main.main(new String[]{ arg }));
		}
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithInvalidPathArg() {

		// Invalid file path
		String[] invalidPathArg = { "-lua #@%", "-java invalid/path" };
		for (String arg : invalidPathArg) {
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Main.main(new String[]{ arg }));
		}
	}

	@Test
	void shouldThrowExceptionWhenApplicationRunWithNoPathArg() {

		// IndexOutOfBoundsException
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Main.main(new String[]{ "-lua" }));
	}

	@Test
	void whenApplicationRunShouldDocumentLuaClasses() {

	}
}
