package io.yooksi.pzlua.tools.parse;

import io.yooksi.pzlua.tools.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaParser {

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
		Main.logger.print((hasFileChanged ? "Changed" : "Unchanged") + ": " + file.getPath());
	}
}
