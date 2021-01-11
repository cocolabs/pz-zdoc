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
package io.yooksi.pz.zdoc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;

class ParseUtilsTest implements UnitTest {

	@Test
	void shouldGetValidRegexMatchedGroups() {

		Pattern pattern = Pattern.compile("(brown)\\s+(dog)\\s+(barks)(\\s+)?");
		Matcher matcher = pattern.matcher("brown dog barks");

		Assertions.assertThrows(IllegalStateException.class,
				() -> ParseUtils.getOptionalMatchedGroup(matcher, 1));

		Assertions.assertTrue(matcher.find());

		Assertions.assertEquals("brown", ParseUtils.getOptionalMatchedGroup(matcher, 1));
		Assertions.assertEquals("dog", ParseUtils.getOptionalMatchedGroup(matcher, 2));
		Assertions.assertEquals("barks", ParseUtils.getOptionalMatchedGroup(matcher, 3));
		Assertions.assertEquals("", ParseUtils.getOptionalMatchedGroup(matcher, 4));

		Assertions.assertThrows(IndexOutOfBoundsException.class,
				() -> ParseUtils.getOptionalMatchedGroup(matcher, 5));
	}
}
