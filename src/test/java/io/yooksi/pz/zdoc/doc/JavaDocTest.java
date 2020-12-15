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
package io.yooksi.pz.zdoc.doc;

import io.yooksi.pz.zdoc.TestWorkspace;
import io.yooksi.pz.zdoc.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class JavaDocTest extends TestWorkspace implements UnitTest {

	JavaDocTest() {
		super("outputLua.lua");
	}

	@Test
	void shouldCorrectlyResolveApiURL() {

		String expected = "https://projectzomboid.com/modding/zombie/inventory/InventoryItem.html";
		URL actual = JavaDoc.resolveApiURL("zombie/inventory/InventoryItem.html");

		Assertions.assertEquals(expected, actual.toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingNonApiUrl() {

		Assertions.assertThrows(IllegalArgumentException.class, () ->
				JavaDoc.resolveApiURL("https://nonapiwebsite.com"));
	}

	@Test
	void shouldCorrectlyResolveApiURLFromOtherURL() {

		String link = "https://projectzomboid.com/";
		Assertions.assertEquals(link, JavaDoc.resolveApiURL(link).toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingApiURLWithInvalidArgument() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> JavaDoc.resolveApiURL('\u0000' + "/p*!h"));
	}

	@Test
	void shouldAttachMissingHTMLFileExtensionWhenParsingApiURL() throws MalformedURLException {

		URL url = new URL("https://projectzomboid.com/modding/zombie/Class.html");
		Assertions.assertEquals(url, JavaDoc.resolveApiURL("zombie/Class"));
	}
}
