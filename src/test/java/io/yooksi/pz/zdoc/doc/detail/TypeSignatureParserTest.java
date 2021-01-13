/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import io.yooksi.pz.zdoc.element.java.JavaClass;

class TypeSignatureParserTest {

	@Test
	void shouldParseUnknownClassAsNullElement() {
		
		Set<String> unknownClassSignatures = Sets.newHashSet(
				"io.yooksi.unknownClass",
				"io.yooksi.unknownClass<java.lang.Object>",
				"io.yooksi.unknownClass<java.lang.Object, java.lang.String>"
		);
		for (String signature : unknownClassSignatures) {
			Assertions.assertNull(TypeSignatureParser.parse(signature));
		}
	}

	@Test
	void shouldParseJavaClassFromSignature() {

		Set<Class<?>> classSignatures = Sets.newHashSet(
				java.lang.String.class,
				java.lang.Object.class,
				java.lang.Integer[].class,
				int.class, int[].class
		);
		for (Class<?> signature : classSignatures)
		{
			JavaClass result = TypeSignatureParser.parse(signature.getName());
			List<JavaClass> typeParameters = Objects.requireNonNull(result).getTypeParameters();

			Assertions.assertEquals(0, typeParameters.size());
			Assertions.assertEquals(signature.getTypeName(), result.getName());
		}
	}

	@Test
	void shouldParseJavaClassTypeParametersFromSignature() {

		Map<String, String[]> classTypeParametersFromSignature = ImmutableMap.<String, String[]>builder()
				.put("java.util.Map<java.lang.String>",
						new String[]{ "java.lang.String" })

				.put("java.util.Map<java.lang.String, java.lang.Object>",
						new String[]{ "java.lang.String", "java.lang.Object" })

				.put("java.util.Map<java.lang.String, java.lang.Class<?>>",
						new String[]{ "java.lang.String", "java.lang.Class<?>" })

				.put("java.util.Map<java.lang.String, java.lang.Class<java.lang.Object>>",
						new String[]{ "java.lang.String", "java.lang.Class<java.lang.Object>" })

				.put("java.util.Map<java.lang.String, java.lang.Class<java.lang.Object<?>>>",
						new String[]{ "java.lang.String", "java.lang.Class<java.lang.Object<?>>" })

				.put("java.util.Map<java.lang.Class<java.lang.Object<?>>, " +
								"java.lang.Class<java.lang.Object<?>>>",
						new String[]{ "java.lang.Class<java.lang.Object<?>>",
								"java.lang.Class<java.lang.Object<?>>" })

				.put("java.util.Map<java.lang.String, java.lang.Object, java.lang.Integer>",
						new String[]{ "java.lang.String", "java.lang.Object", "java.lang.Integer" })

				.put("java.util.Map<java.lang.Class<java.lang.Class<?>>, " +
								"java.util.Map<java.lang.Class<?>, java.lang.Object>, " +
								"java.util.Map<java.lang.Object, java.lang.Class<?>>>",

						new String[]{ "java.lang.Class<java.lang.Class<?>>",
								"java.util.Map<java.lang.Class<?>, java.lang.Object>",
								"java.util.Map<java.lang.Object, java.lang.Class<?>>" }).build();

		for (Map.Entry<String, String[]> entry : classTypeParametersFromSignature.entrySet())
		{
			String signature = entry.getKey();
			String[] expected = entry.getValue();

			JavaClass result = TypeSignatureParser.parse(signature);
			List<JavaClass> typeParameters = Objects.requireNonNull(result).getTypeParameters();

			Assertions.assertEquals(expected.length, typeParameters.size());
			for (int i = 0; i < typeParameters.size(); i++) {
				Assertions.assertEquals(expected[i], typeParameters.get(i).toString());
			}
		}
	}
}
