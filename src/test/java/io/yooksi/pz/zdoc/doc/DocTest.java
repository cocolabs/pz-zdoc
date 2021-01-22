/*
 * Copyright (C) 2020 Matthew Cain
 * ZomboidDoc - Lua library compiler for Project Zomboid
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
package io.yooksi.pz.zdoc.doc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Tag;

@Tag("doc")
public abstract class DocTest {

	protected static final ZomboidAPIDoc DOCUMENT;
	protected static final Path DOCUMENT_PATH;

	static
	{
		try {
			URL resource = DocTest.class.getClassLoader().getResource("Test.html");
			DOCUMENT_PATH = new File(Objects.requireNonNull(resource).toURI()).toPath();
			DOCUMENT = ZomboidAPIDoc.getLocalPage(DOCUMENT_PATH);
			DOCUMENT.getDocument().setBaseUri(ZomboidAPIDoc.resolveURL("zombie/Test.html").toString());
		}
		catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
