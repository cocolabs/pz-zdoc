package io.yooksi.pz.luadoc;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.yooksi.pz.luadoc.parse.JavaDocParser;
import io.yooksi.pz.luadoc.parse.LuaParser;

public class Main {

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
			Path docPath, outputDir;
			java.util.List<Path> paths;
			try {
				docPath = Paths.get(args[1]);
				outputDir = args.length >= 3 ? Paths.get(args[2]) : null;
				paths = Files.walk(Paths.get(docPath.toString())).filter(Files::isRegularFile)
						.collect(Collectors.toCollection(ArrayList::new));
			}
			catch (IndexOutOfBoundsException e) {
				throw new RuntimeException("No file path supplied", e);
			}
			int docsWritten = 0;
			for (Path path : paths)
			{
				if (LuaParser.documentLuaFile(path.toFile(), outputDir))
				{
					System.out.println("- Documented lua file " + path);
					docsWritten += 1;
				}
			}
			System.out.printf("Documented %d classes in %s%n", docsWritten, docPath.toString());
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
			if (Utils.isValidUrl(source)) {
				JavaDocParser.loadURL(source).convertJavaToLuaDoc(path);
			}
			else if (Utils.isValidPath(source)) {
				JavaDocParser.loadFile(source).convertJavaToLuaDoc(path);
			}
			else throw new IllegalArgumentException("\"" + source + "\" is not a valid file path or URL");
		}
		else throw new IllegalArgumentException("Unknown application argument: " + opArg);
	}
}
