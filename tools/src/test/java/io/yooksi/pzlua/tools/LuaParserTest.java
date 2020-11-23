package io.yooksi.pzlua.tools;

import io.yooksi.pzlua.tools.lang.EmmyLuaAnnotation;
import io.yooksi.pzlua.tools.parse.LuaParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class LuaParserTest {

    @Test
    public void shouldDocumentSampleLuaFile(@TempDir Path dir) throws IOException {

        File sample = createTempFile(dir, "sampleLua.lua");
        String[] lines = {
                "",
                "--*******************",
                "-- this is a comment",
                "--*******************",
                "",
                "sampleLua = luaClass:derive()"
        };
        FileUtils.writeLines(sample, Arrays.asList(lines));
        Assertions.assertEquals(6, FileUtils.readLines(sample, Charset.defaultCharset()).size());

        LuaParser.documentLuaFile(sample);

        List<String> linesList = FileUtils.readLines(sample, Charset.defaultCharset());
        Assertions.assertEquals(7, linesList.size());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), linesList.get(5));
    }

    @Test
    public void shouldNotThrowExceptionWhenParsingTopLineLuaFile(@TempDir Path dir) throws IOException {

        File sample = createTempFile(dir, "sampleLua.lua");
        String[] lines = { "sampleLua = luaClass:derive()" };
        FileUtils.writeLines(sample, Arrays.asList(lines));

        // IndexOutOfBoundsException
        Assertions.assertDoesNotThrow(() -> LuaParser.documentLuaFile(sample));
    }

    @Test
    public void shouldOverwriteExistingLuaAnnotation(@TempDir Path dir) throws IOException {

        File sample = createTempFile(dir, "sampleLua.lua");
        String[] write = {
                "--- This is a sample comment",
                "---@class otherSampleLua",
                "sampleLua = luaClass:derive()"
        };
        FileUtils.writeLines(sample, Arrays.asList(write));

        LuaParser.documentLuaFile(sample);

        List<String> read = FileUtils.readLines(sample, Charset.defaultCharset());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), read.get(1));

    }

    private static File createTempFile(Path dir, String name) throws IOException {

        File temp = dir.resolve(name).toFile();
        Assertions.assertTrue(temp.createNewFile());
        Assertions.assertTrue(temp.exists());
        return temp;
    }
}
