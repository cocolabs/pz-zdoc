package io.yooksi.pzlua.tools;

import io.yooksi.pzlua.tools.parse.LuaParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static final Logger logger = new Logger();

    public static void main(String[] args) throws IOException {

        Files.walk(Paths.get("src/lua")).filter(Files::isRegularFile)
                .forEach(path -> {
                    try { LuaParser.documentLuaFile(path.toFile()); }
                    catch (IOException e) { throw new RuntimeException(e); }
                });
    }
}
