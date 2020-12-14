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
package io.yooksi.pz.zdoc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

public class Utils {

	/**
	 * @return {@code true} if file denoted by given filename is a lua file.
	 *
	 * @throws IllegalArgumentException <b>Windows only:</b> filename parameter is, in fact, the identifier
	 * 		of an Alternate Data Stream, for example "foo.exe:bar.txt".
	 */
	public static boolean isLuaFile(String filename) {
		return FilenameUtils.getExtension(filename).equals("lua");
	}

	/**
	 * @return {@code true} if file denoted by given path is a lua file.
	 * @throws NullPointerException if given path has no elements.
	 */
	public static boolean isLuaFile(Path path) {
		return isLuaFile(path.getFileName().toString());
	}

	/**
	 * @return {@code true} if the given string can be parsed into a valid {@link Path} object.
	 */
	public static boolean isValidPath(String path) {
		try {
			Paths.get(path);
		}
		catch (InvalidPathException e) {
			return false;
		}
		return true;
	}

	public static @Nullable Path getPathOrNull(String path) {
		try {
			return Paths.get(path);
		}
		catch (InvalidPathException e) {
			return null;
		}
	}

	public static URL getURL(String link) {
		try {
			return new URL(link);
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static @Nullable URL getURLOrNull(String link) {
		try {
			return new URL(link);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	public static URL getURL(URL root, String... link) {
		try {
			StringBuilder sb = new StringBuilder();
			Arrays.stream(link).forEach(l -> sb.append(l).append('/'));
			return new URL(root, sb.substring(0, sb.length() - 1));
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return {@code true} if the given {@code Class} object is declared in Java JDK.
	 * 		Note that this method only checks if the target class was loaded using the same
	 *        {@link ClassLoader} used to load Java JDK, and cannot guarantee that the target
	 * 		class is declared in Java JDK, but is good enough in most cases.
	 *
	 * @see <a href="https://stackoverflow.com/a/8703709/5759072">Code snippet on StackOverflow</a>
	 */
	public static boolean isJavaJDKClass(Class<?> clazz) {
		return clazz.getClassLoader() == "".getClass().getClassLoader();
	}
}
