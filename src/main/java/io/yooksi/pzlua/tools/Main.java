package io.yooksi.pzlua.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.yooksi.pzlua.tools.parse.JavaDocParser;
import io.yooksi.pzlua.tools.parse.LuaParser;

public class Main {

	public static final Path PROJECT_DIR = Paths.get(System.getProperty("user.dir"));
	public static final Logger logger = new Logger();

	/**
	 * @throws InvalidPathException if malformed path passed as argument
	 * @throws NoSuchFileException if unable to find file under argument path
	 * @throws IndexOutOfBoundsException if no path argument supplied
	 */
	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			throw new IllegalArgumentException("No application argument supplied");
		}
		Pattern argRegex = Pattern.compile("^\\s*+-(\\w+)\\s*$");

		String rawOpArg = args[0];
		Matcher matcher = argRegex.matcher(rawOpArg);

		if (!matcher.find()) {
			throw new IllegalArgumentException("Malformed application argument: " + rawOpArg);
		}
		String opArg = matcher.group(1);
		if (opArg.equals("lua"))
		{
			Path path;
			java.util.List<Path> paths;
			try {
				path = Paths.get(args[1]);
				paths = Files.walk(Paths.get(path.toString())).filter(Files::isRegularFile)
						.collect(Collectors.toCollection(ArrayList::new));
			}
			catch (IndexOutOfBoundsException e) {
				throw new RuntimeException("No file path supplied", e);
			}
			int docsWritten = 0;
			for (Path docPath : paths)
			{
				if (LuaParser.documentLuaFile(docPath.toFile()))
				{
					System.out.println("- Documented lua file " + docPath.relativize(PROJECT_DIR));
					docsWritten += 1;
				}
			}
			System.out.printf("Documented %d classes in %s%n", docsWritten, path.toString());
		}
		// document java to lua
		else if (opArg.equals("java"))
		{
			Path path;
			try {
				path = Paths.get(args[1]);
				if (!path.toFile().exists()) {
					throw new NoSuchFileException(path.toString(), null, "Output file not found");
				}
			} catch (IndexOutOfBoundsException e) {
				throw new RuntimeException("No output file path supplied", e);
			}
			String source = args.length >= 3 ? args[2] : JavaDocParser.PZ_API_GLOBAL_URL;
			if (isValidUrl(source)) {
				JavaDocParser.loadURL(source).convertJavaToLuaDoc(path);
			}
			else if (isValidPath(source)) {
				JavaDocParser.loadFile(source).convertJavaToLuaDoc(path);
			}
			else throw new IllegalArgumentException("\"" + source + "\" is not a valid file path or URL");
		}
		else throw new IllegalArgumentException("Unknown application argument: " + opArg);
	}

	public static boolean isValidUrl(String url) {

		try { new URL(url); }
		catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	public static boolean isValidPath(String path) {

		try { Paths.get(path); }
		catch (InvalidPathException e) {
			return false;
		}
		return true;
	}
}
