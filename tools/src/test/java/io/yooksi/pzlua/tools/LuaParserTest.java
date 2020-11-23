package io.yooksi.pzlua.tools;

import io.yooksi.pzlua.tools.lang.EmmyLuaAnnotation;
import io.yooksi.pzlua.tools.parse.LuaParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class LuaParserTest extends TestWorkspace {

    LuaParserTest() throws IOException {
        super("sampleLua.lua");
    }

    @Test
    void shouldDocumentSampleLuaFile() throws IOException {

        String[] lines = {
                "",
                "--*******************",
                "-- this is a comment",
                "--*******************",
                "",
                "sampleLua = luaClass:new()"
        };
        FileUtils.writeLines(file, Arrays.asList(lines));
        Assertions.assertEquals(6, FileUtils.readLines(file, Charset.defaultCharset()).size());

        LuaParser.documentLuaFile(file);

        List<String> linesList = FileUtils.readLines(file, Charset.defaultCharset());
        Assertions.assertEquals(7, linesList.size());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), linesList.get(5));
    }

    @Test
    void shouldNotThrowExceptionWhenParsingTopLineLuaFile() throws IOException {

        String[] lines = { "sampleLua = luaClass:derive()" };
        FileUtils.writeLines(file, Arrays.asList(lines));

        // IndexOutOfBoundsException
        Assertions.assertDoesNotThrow(() -> LuaParser.documentLuaFile(file));
    }

    @Test
    void shouldOverwriteExistingLuaAnnotation() throws IOException {

        String[] write = {
                "--- This is a sample comment",
                "---@class otherSampleLua",
                "sampleLua = luaClass:new()"
        };
        FileUtils.writeLines(file, Arrays.asList(write));
        LuaParser.documentLuaFile(file);

        List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), read.get(1));

    }

    @Test
    void shouldReadAnnotationsWithWhitespaces() throws IOException {

        String[] write = {
                "---  @class otherSampleLua",
                "sampleLua = luaClass:new()"
        };
        FileUtils.writeLines(file, Arrays.asList(write));
        LuaParser.documentLuaFile(file);

        List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), read.get(0));
    }

    @Test
    void shouldParseAnnotationIncludeParentType() throws IOException {

        String[] write = {
                "---@class sampleLua",
                "sampleLua = luaClass:new()"
        };
        FileUtils.writeLines(file, Arrays.asList(write));
        LuaParser.documentLuaFile(file);

        List<String> read = FileUtils.readLines(file, Charset.defaultCharset());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua"), read.get(0));

        write[1] = "sampleLua = luaClass:derive()";
        FileUtils.writeLines(file, Arrays.asList(write), false);
        LuaParser.documentLuaFile(file);

        read = FileUtils.readLines(file, Charset.defaultCharset());
        Assertions.assertEquals(EmmyLuaAnnotation.CLASS.create("sampleLua", "luaClass"), read.get(0));
    }

}
