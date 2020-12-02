package io.yooksi.pz.luadoc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

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
	 * @return {@code true} if the given string can be parsed into a valid {@link URL} object.
	 */
	public static boolean isValidUrl(String url) {

		try { new URL(url); }
		catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	/**
	 * @return {@code true} if the given string can be parsed into a valid {@link Path} object.
	 */
	public static boolean isValidPath(String path) {

		try { Paths.get(path); }
		catch (InvalidPathException e) {
			return false;
		}
		return true;
	}

	public static URL getURL(String link) {
		try {
			return new URL(link);
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
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
}
