package io.yooksi.pzlua.tools;

import io.yooksi.pzlua.tools.parse.LuaParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {

    public static final Logger logger = new Logger();

    public static void main(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("--document-lua"))
            {
                Path path; java.util.List<Path> paths;
                try {
                    path = Paths.get(args[i + 1]);
                    paths = Files.walk(Paths.get(path.toString())).filter(Files::isRegularFile)
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                catch(InvalidPathException e) {
                    throw new RuntimeException("Invalid lua file path", e);
                }
                catch(IndexOutOfBoundsException e) {
                    throw new RuntimeException("No lua file path supplied", e);
                }
                int docsWritten = 0;
                for (Path docPath : paths) {
                    if (LuaParser.documentLuaFile(docPath.toFile())) {
                        docsWritten += 1;
                    }
                }
                System.out.printf("Documented %d classes in %s%n", docsWritten, path.toString());
            }
        }
    }
}
