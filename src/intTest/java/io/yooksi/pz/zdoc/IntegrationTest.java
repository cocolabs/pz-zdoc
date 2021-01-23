/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2021 Matthew Cain
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
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@Tag("integration")
public interface IntegrationTest {

	@BeforeAll
	static void initializeIntegrationTest() throws IOException {

		File targetFile = new File("serialize.lua");
		if (!targetFile.exists())
		{
			try (InputStream iStream = Main.CLASS_LOADER.getResourceAsStream("serialize.lua"))
			{
				if (iStream == null) {
					throw new IllegalStateException("Unable to find serialize.lua resource file");
				}
				FileUtils.copyToFile(iStream, targetFile);
			}
		}
	}

	@AfterAll
	static void finalizeIntegrationTest() {

		File targetFile = new File("serialize.lua");
		if (!targetFile.delete()) {
			targetFile.deleteOnExit();
		}
	}
}
