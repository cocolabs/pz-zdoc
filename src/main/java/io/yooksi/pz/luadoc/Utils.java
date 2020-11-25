package io.yooksi.pz.luadoc;

import org.apache.commons.io.FilenameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

	/**
	 * @return {@code true} if file denoted by given filename is a lua file
	 */
	public static boolean isLuaFile(String filename) {
		return FilenameUtils.getExtension(filename).equals("lua");
	}

	/**
	 * @return {@code true} if file denoted by given path is a lua file
	 * @throws NullPointerException if given path has no elements
	 */
	public static boolean isLuaFile(Path path) {
		return isLuaFile(path.getFileName().toString());
	}

	public static boolean isValidUrl(String url) {

		try { new URL(url); }
		catch (MalformedURLException e) {
			return false;
		} return true;
	}

	public static boolean isValidPath(String path) {

		try { Paths.get(path); }
		catch (InvalidPathException e) {
			return false;
		} return true;
	}
}
