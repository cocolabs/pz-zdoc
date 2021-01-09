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
package io.yooksi.pz.zdoc.element;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;

public class JavaMethodTest implements UnitTest {

	@Test
	void shouldCorrectlyParseComplexJavaMethod() {

		JavaMethod.Parser parser = JavaMethod.Parser.create("static KahluaTable " +
				"transformIntoKahluaTable(java.util.HashMap<java.lang.Object," +
				"java.lang.Object> map, Object index)");

		JavaMethod method = parser.parse();
		Assertions.assertNotNull(method);

		Assertions.assertEquals("static", method.modifier);
		Assertions.assertEquals("KahluaTable", method.returnType);
		Assertions.assertEquals("transformIntoKahluaTable", method.name);

		Assertions.assertEquals(2, method.params.length);
		Assertions.assertEquals("map", method.params[0].getName(false));
		Assertions.assertEquals("HashMap<Object,Object>", method.params[0].getType(false));
	}
}
