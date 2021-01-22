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
package io.yooksi.pz.zdoc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.ClassUtils;

import io.yooksi.pz.zdoc.Main;

@SuppressWarnings("WeakerAccess")
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

	public static URL getURLBase(URL url) {
		return getURL(url.getProtocol() + "://" + url.getHost());
	}

	public static Class<?> getClassForName(String name) throws ClassNotFoundException {
		return ClassUtils.forName(name, null);
	}

	public static Properties getProperties(String path) throws IOException {

		Properties properties = new Properties();
		try {
			URL resource = Main.CLASS_LOADER.getResource(path);
			File fLuaProperties = new File(Objects.requireNonNull(resource).toURI());
			try (FileInputStream fis = new FileInputStream(fLuaProperties)) {
				properties.load(fis);
			}
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		catch (FileNotFoundException e) {
			return null;
		}
		return properties;
	}
}
