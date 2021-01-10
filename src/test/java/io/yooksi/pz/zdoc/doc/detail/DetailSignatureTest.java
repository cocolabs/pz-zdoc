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
package io.yooksi.pz.zdoc.doc.detail;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DetailSignatureTest implements UnitTest {

	@Test
	void shouldParseClassTypeParametersFromSignature() {

		Map<String, String[]> classTypeParametersFromSignature = Map.of(
				"java.util.Map<java.lang.String>",
				new String[]{ "java.lang.String" },

				"java.util.Map<java.lang.String, java.lang.Object>",
				new String[]{ "java.lang.String", "java.lang.Object" },

				"java.util.Map<java.lang.String, java.lang.Class<?>>",
				new String[]{ "java.lang.String", "java.lang.Class<?>" },

				"java.util.Map<java.lang.String, java.lang.Class<java.lang.Object>>",
				new String[]{ "java.lang.String", "java.lang.Class<java.lang.Object>" },

				"java.util.Map<java.lang.String, java.lang.Class<java.lang.Object<?>>>",
				new String[]{ "java.lang.String", "java.lang.Class<java.lang.Object<?>>" },

				"java.util.Map<java.lang.Class<java.lang.Object<?>>, java.lang.Class<java.lang.Object<?>>>",
				new String[]{
						"java.lang.Class<java.lang.Object<?>>", "java.lang.Class<java.lang.Object<?>>"
				}
		);
		for (Map.Entry<String, String[]> entry : classTypeParametersFromSignature.entrySet())
		{
			String signature = entry.getKey();
			String[] expected = entry.getValue();

			JavaClass result = DetailSignature.parseClassSignature(signature);
			List<JavaClass> typeParameters = Objects.requireNonNull(result).getTypeParameters();

			Assertions.assertEquals(expected.length, typeParameters.size());
			for (int i = 0; i < typeParameters.size(); i++) {
				Assertions.assertEquals(expected[i], typeParameters.get(i).toString());
			}
		}
	}
}
