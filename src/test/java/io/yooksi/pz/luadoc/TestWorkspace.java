package io.yooksi.pz.luadoc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
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

	protected void createSampleLuaFile() throws IOException {

		String[] lines = {
				"",
				"--*******************",
				"-- this is a comment",
				"--*******************",
				"",
				"sampleLua = luaClass:new()"
		};
		FileUtils.writeLines(file, Arrays.asList(lines));
		Assertions.assertEquals(6, FileUtils.readLines(file, Charset.defaultCharset()).size());
	}
}
