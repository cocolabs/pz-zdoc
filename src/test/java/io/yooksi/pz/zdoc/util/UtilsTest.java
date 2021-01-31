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
package io.yooksi.pz.zdoc.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void shouldResolveUrlWithMultipleDirectories() throws MalformedURLException {

		URL url = new URL("https://worldwideweb.com");
		URL result = Utils.getURL(url, "one", "two", "three");

		String expected = "https://worldwideweb.com/one/two/three";
		Assertions.assertEquals(expected, result.toString());
	}

	@Test
	void shouldGetValidClassForObjectClassName() throws ClassNotFoundException {

		Assertions.assertEquals(Object.class, Utils.getClassForName(Object.class.getName()));
		Assertions.assertThrows(ClassNotFoundException.class, () -> Utils.getClassForName("nonExistingClass"
		));
	}

	@Test
	void shouldGetValidArrayClassForObjectClassName() throws ClassNotFoundException {

		Assertions.assertEquals(String[].class, Utils.getClassForName("java.lang.String[]"));
		Assertions.assertEquals(String[][].class, Utils.getClassForName("java.lang.String[][]"));
	}

	@Test
	void shouldGetValidClassForPrimitiveClassName() throws ClassNotFoundException {

		Assertions.assertEquals(boolean.class, Utils.getClassForName("boolean"));
		Assertions.assertEquals(byte.class, Utils.getClassForName("byte"));
		Assertions.assertEquals(char.class, Utils.getClassForName("char"));
		Assertions.assertEquals(short.class, Utils.getClassForName("short"));
		Assertions.assertEquals(int.class, Utils.getClassForName("int"));
		Assertions.assertEquals(long.class, Utils.getClassForName("long"));
		Assertions.assertEquals(float.class, Utils.getClassForName("float"));
		Assertions.assertEquals(double.class, Utils.getClassForName("double"));
		Assertions.assertEquals(void.class, Utils.getClassForName("void"));
	}
}
