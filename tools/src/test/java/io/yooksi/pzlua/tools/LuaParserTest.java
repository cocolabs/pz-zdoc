package io.yooksi.pzlua.tools;

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

        File temp = dir.resolve("temp.txt").toFile();
        Assertions.assertTrue(temp.createNewFile());
        Assertions.assertTrue(temp.exists());
        String[] lines = {
                "",
                "--**************",
                "-- this is some text",
                "--**************",
                "",
                "temp = abcd"
        };
        FileUtils.writeLines(temp, Arrays.asList(lines));
        Assertions.assertEquals(6, FileUtils.readLines(temp, Charset.defaultCharset()).size());

        io.yooksi.pzlua.tools.parse.LuaParser.documentLuaFile(temp);

        List<String> linesList = FileUtils.readLines(temp, Charset.defaultCharset());
        Assertions.assertEquals(7, linesList.size());
        Assertions.assertEquals("---@class temp", linesList.get(5));
    }
}
