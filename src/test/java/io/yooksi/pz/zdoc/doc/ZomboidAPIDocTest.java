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

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.util.Utils;

class ZomboidAPIDocTest extends DocTest implements UnitTest {

	@Test
	void shouldCorrectlyResolveZomboidDocApiURL() {

		String expected = "https://projectzomboid.com/modding/zombie/inventory/InventoryItem.html";
		URL actual = ZomboidAPIDoc.resolveURL("zombie/inventory/InventoryItem.html");

		Assertions.assertEquals(expected, actual.toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingZomboidDocNonApiUrl() {

		Assertions.assertThrows(IllegalArgumentException.class, () ->
				ZomboidAPIDoc.resolveURL("https://nonapiwebsite.com"));
	}

	@Test
	void shouldCorrectlyResolveZomboidDocApiURLFromOtherURL() {

		String link = "https://projectzomboid.com/";
		Assertions.assertEquals(link, ZomboidAPIDoc.resolveURL(link).toString());
	}

	@Test
	void shouldThrowExceptionWhenResolvingZomboidDocApiURLWithInvalidArgument() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> ZomboidAPIDoc.resolveURL('\u0000' + "/p*!h"));
	}

	@Test
	void shouldAttachMissingHTMLFileExtensionWhenParsingZomboidDocApiURL() {

		URL url = Utils.getURL("https://projectzomboid.com/modding/zombie/Class.html");
		Assertions.assertEquals(url, ZomboidAPIDoc.resolveURL("zombie/Class"));
	}

	@Test
	void shouldCorrectlyValidateApiURL() {

		// absolute url to api html page
		URL url = Utils.getURL("https://projectzomboid.com/modding/zombie/Class.html");
		Assertions.assertTrue(ZomboidAPIDoc.isValidURL(url));

		// absolute url to api directory
		url = Utils.getURL("https://projectzomboid.com/modding/zombie/");
		Assertions.assertTrue(ZomboidAPIDoc.isValidURL(url));

		url = Utils.getURL("https://projectzomboid.com/modding/");
		Assertions.assertTrue(ZomboidAPIDoc.isValidURL(url));

		// url containing only protocol and host
		url = Utils.getURL("https://projectzomboid.com/");
		Assertions.assertTrue(ZomboidAPIDoc.isValidURL(url));

		url = Utils.getURL("https://project.com/");
		Assertions.assertFalse(ZomboidAPIDoc.isValidURL(url));

		url = Utils.getURL("https://project.com/test");
		Assertions.assertFalse(ZomboidAPIDoc.isValidURL(url));
	}

	@Test
	void shouldCorrectlyResolveApiURLPath() {

		assertValidResolvedApiURLPath(
				"https://projectzomboid.com/modding/zombie/Class.html",
				"zombie/Class.html"
		);
		assertValidResolvedApiURLPath(
				"https://projectzomboid.com/modding/zombie/",
				"zombie/"
		);
		assertValidResolvedApiURLPath(
				"https://projectzomboid.com/modding/",
				""
		);
		assertValidResolvedApiURLPath(
				"https://projectzomboid.com/",
				"../"
		);
		Assertions.assertThrows(RuntimeException.class, () ->
				assertValidResolvedApiURLPath("https://test.io/", "")
		);
	}

	@Test
	void shouldConstructValidZomboidDocFromLocalAPIPage() {

		ZomboidAPIDoc doc = DocTest.DOCUMENT;

		Assertions.assertNotNull(doc.getDocument());
		Assertions.assertEquals("Test.html", doc.getName());
	}

	@TestOnly
	private void assertValidResolvedApiURLPath(String actualUrl, String expectedUrl) {

		String sActual = Utils.getURL(actualUrl).toString();
		Path pActual = Paths.get(ZomboidAPIDoc.resolveURLPath(sActual).toString());
		Assertions.assertEquals(Paths.get(expectedUrl), pActual);
	}
}
