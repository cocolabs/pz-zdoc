package io.yooksi.pz.luadoc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
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
