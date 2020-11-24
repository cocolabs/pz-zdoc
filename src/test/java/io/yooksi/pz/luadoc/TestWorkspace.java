package io.yooksi.pz.luadoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class TestWorkspace {

	protected final String filename;
	protected File dir, file;

	public TestWorkspace(String filename) {
		this.filename = filename;
	}

	@BeforeEach
	void createTempFile(@TempDir Path dir) throws IOException {

		this.dir = dir.toFile();
		file = dir.resolve(filename).toFile();
		Assertions.assertTrue(file.createNewFile());
		Assertions.assertTrue(file.exists());
	}
}
