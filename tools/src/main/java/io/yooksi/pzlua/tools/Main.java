package io.yooksi.pzlua.tools;

import io.yooksi.pzlua.tools.parse.JavaDocParser;
import io.yooksi.pzlua.tools.parse.LuaParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

	public static final Path PROJECT_DIR = Paths.get(System.getProperty("user.dir"));
    public static final Logger logger = new Logger();

    public static void main(String[] args) throws IOException {

    	if (args.length == 0) {
    		throw new IllegalArgumentException("No application argument supplied");
		}
		Pattern argRegex = Pattern.compile("\\s*+-(\\w+)\\s*");
        for (int i = 0; i < args.length; i++)
        {
        	String rawOpArg = args[i];
        	Matcher matcher = argRegex.matcher(rawOpArg);

        	if (!matcher.find()) {
				throw new IllegalArgumentException("Malformed application argument: " + rawOpArg);
			}
        	String opArg = matcher.group(1);
        	if (opArg.equals("-lua"))
            {
                Path path; java.util.List<Path> paths;
                try {
                    path = Paths.get(args[i + 1]);
                    paths = Files.walk(Paths.get(path.toString())).filter(Files::isRegularFile)
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                catch(InvalidPathException e) {
                    throw new RuntimeException("Invalid file path: " + args[i + 1], e);
                }
                catch(IndexOutOfBoundsException e) {
                    throw new RuntimeException("No file path supplied", e);
                }
                int docsWritten = 0;
                for (Path docPath : paths) {
                    if (LuaParser.documentLuaFile(docPath.toFile())) {
                        docsWritten += 1;
                    }
                }
                System.out.printf("Documented %d classes in %s%n", docsWritten, path.toString());
            }
            // document java to lua
            else if (opArg.equals("-java"))
            {
                Path path;
                try {
                    path = Paths.get(args[i + 1]);
                }
                catch(InvalidPathException e) {
                    throw new RuntimeException("Invalid output file path: " + args[i + 1], e);
                }
                catch(IndexOutOfBoundsException e) {
                    throw new RuntimeException("No output file path supplied", e);
                }
                String sUrl = args.length < i + 2 ? args[i + 2] : JavaDocParser.PZ_API_GLOBAL_URL;
                URL url = new URL(JavaDocParser.PZ_API_URL.concat(sUrl));

				JavaDocParser.create(url.toString()).convertJavaToLuaDoc(path);
            }
            else throw new IllegalArgumentException("Unknown application argument: " + opArg);
        }
    }
}
