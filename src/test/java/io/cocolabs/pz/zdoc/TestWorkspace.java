/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.StringUtils;

@SuppressWarnings({ "WeakerAccess", "unused" })
public abstract class TestWorkspace {

	protected final String filename;
	protected File dir, file;

	public TestWorkspace(String filename) {
		this.filename = filename;
	}

	public TestWorkspace() {
		this.filename = "test.file";
	}

	@BeforeEach
	void createTempFile(@TempDir Path dir) throws IOException {

		this.dir = dir.toFile();
		file = dir.resolve(filename).toFile();
		if (!StringUtils.isBlank(filename)) {
			Assertions.assertTrue(file.createNewFile());
		}
	}

	protected List<String> readFile() throws IOException {
		return FileUtils.readLines(file, Main.CHARSET);
	}
}
