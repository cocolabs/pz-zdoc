/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc;

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
