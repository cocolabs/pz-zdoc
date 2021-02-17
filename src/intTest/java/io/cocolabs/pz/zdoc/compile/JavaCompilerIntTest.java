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
package io.cocolabs.pz.zdoc.compile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import io.cocolabs.pz.zdoc.IntegrationTest;

class JavaCompilerIntTest implements IntegrationTest {

	private static final File EXPOSED_JAVA;

	static
	{
		try {
			ClassLoader cl = JavaCompilerIntTest.class.getClassLoader();
			EXPOSED_JAVA = new File(
					Objects.requireNonNull(cl.getResource("exposed.txt")).toURI()
			);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void shouldGetAllExposedJavaClasses() throws ReflectiveOperationException, IOException {

		List<String> expectedExposedElements = FileUtils.readLines(EXPOSED_JAVA, Charset.defaultCharset());
		HashSet<Class<?>> actualExposedElements = JavaCompiler.getExposedJava();

		for (Class<?> actualExposedElement : actualExposedElements)
		{
			if (!expectedExposedElements.contains(actualExposedElement.getName()))
			{
				String message = "Did not find exposed Java class";
				throw new AssertionFailedError(message, null, actualExposedElement);
			}
		}
	}

	/**
	 * Ensure that {@link NoClassDefFoundError} and {@link ClassNotFoundException}
	 * exceptions are not thrown, which happens when JDK classes are not found.
	 */
	@Test
	void shouldNotThrowClassNotFoundExceptionWhenReadingExposedJavaClasses() throws ReflectiveOperationException {

		for (Class<?> exposedClass : JavaCompiler.getExposedJava())
		{
			Assertions.assertDoesNotThrow(exposedClass::getDeclaredConstructors);
			Assertions.assertDoesNotThrow(exposedClass::getDeclaredFields);
			Assertions.assertDoesNotThrow(exposedClass::getDeclaredMethods);
		}
	}
}
