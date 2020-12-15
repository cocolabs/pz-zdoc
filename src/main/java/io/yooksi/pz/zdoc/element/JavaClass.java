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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.lang3.tuple.Pair;

import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.logger.Logger;

/**
 * This class represents a parsed non-java-native class reference.
 */
public class JavaClass implements MemberClass {

	private final String name;
	private final URL apiDocPage;

	public JavaClass(String name, String apiDocPage) {
		this.name = name;
		try {
			this.apiDocPage = new URL(apiDocPage);
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public JavaClass(String name, URL apiDocPage) {
		this.name = name;
		this.apiDocPage = apiDocPage;
	}

	private static Pair<String, String> parseClass(Class<?> clazz) {

		String signature = clazz.toString();
		String[] elements = signature.split("\\s");
		if (elements.length != 2)
		{
			throw new IllegalStateException(String.format(
					"Unexpected class (%s) signature: %s", clazz.getSimpleName(), signature));
		}
		String classPath = elements[1].replaceAll("\\.", "\\/");
		String className = Paths.get(classPath).getFileName().toString();
		if (clazz.isMemberClass())
		{
			if (!signature.contains("$")) {
				Logger.warn(String.format("Expected to find \"$\" symbol denoting a member class " +
						"(%s) in class signature: %s", clazz.getSimpleName(), signature));
			}
			classPath = classPath.replaceAll("\\$", ".");
			className = className.replace("$", "_");
		}
		return Pair.of(className, classPath);
	}

	public static JavaClass createClass(Class<?> clazz, URL docPage) {

		Pair<String, String> data = parseClass(clazz);
		return new JavaClass(data.getKey(), docPage);
	}

	/**
	 * Create and return a new {@code JavaClass} instance that represents a given
	 * Project Zomboid {@code Class}. The given {@code Class} object is assumed to be a
	 * class that originates from a Project Zomboid library and this method does not
	 * take responsibility for validating its origin.
	 *
	 * @param clazz target {@code Class} object to create a {@code JavaClass} from.
	 * @return new {@code JavaClass} instance with a location that points to an API URL
	 * 		that corresponds to the given {@code Class} object being part of PZ code base.
	 *
	 * @throws IllegalStateException if encountered an unexpected class signature.
	 */
	public static JavaClass createZomboidClass(Class<?> clazz) {

		Pair<String, String> data = parseClass(clazz);
		return new JavaClass(data.getKey(), JavaDoc.resolveApiURL(data.getValue()));
	}

	public URL getApiDocPage() {
		return apiDocPage;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, apiDocPage.toString());
	}
}
