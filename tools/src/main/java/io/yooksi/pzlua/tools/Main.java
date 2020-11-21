package io.yooksi.pzlua.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final Logger logger = new Logger();

    public static void documentLuaFile(File file) throws IOException {

        String filename = FilenameUtils.getBaseName(file.getName());
        boolean hasFileChanged = false;

        List<String> linesToWrite = new ArrayList<>();
        for (String line : FileUtils.readLines(file, Charset.defaultCharset()))
        {
            Pattern pattern = Pattern.compile("^\\s*" + filename + "\\s+=");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find())
            {
                hasFileChanged = true;
                linesToWrite.add("---@class " + filename);
            }
            linesToWrite.add(line);
        }
        if (hasFileChanged) {
            FileUtils.writeLines(file, linesToWrite, false);
        }
        logger.print((hasFileChanged ? "Changed" : "Unchanged") + ": " + file.getPath());
    }

    public static void documentLuaFile(Path path) {

        try {
            documentLuaFile(path.toFile());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        Files.walk(Paths.get("src/lua")).filter(Files::isRegularFile)
                .forEach(Main::documentLuaFile);
    }
}
