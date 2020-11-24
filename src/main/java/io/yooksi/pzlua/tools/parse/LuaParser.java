package io.yooksi.pzlua.tools.parse;

import io.yooksi.pzlua.tools.Main;
import io.yooksi.pzlua.tools.lang.EmmyLua;
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

	private static final Pattern DERIVED_CLASS = Pattern.compile("=\\s*(\\w+):derive\\(");

	public static boolean documentLuaFile(File file) throws IOException {

		String filename = FilenameUtils.getBaseName(file.getName());
		boolean hasFileChanged = false;

		List<String> linesToWrite = new ArrayList<>();
		List<String> linesToRead = FileUtils.readLines(file, Charset.defaultCharset());
		for (int i = 0; i < linesToRead.size(); i++)
		{
			String line = linesToRead.get(i);
			Pattern pattern = Pattern.compile("^\\s*" + filename + "\\s+=");
			if (pattern.matcher(line).find())
			{
				if (i > 0)
				{ // make sure we are not on the first line
					String prevLine = linesToRead.get(i - 1);
					if (EmmyLua.CLASS.isAnnotation(prevLine))
					{
						linesToWrite.remove(i - 1);
					}
				}
				String annotation = EmmyLua.CLASS.create(filename);
				Matcher matcher = DERIVED_CLASS.matcher(line);
				if (matcher.find())
				{
					annotation += " : " + matcher.group(1);
				}
				linesToWrite.add(annotation);
				hasFileChanged = true;
			}
			linesToWrite.add(line);
		}
		if (hasFileChanged)
		{
			FileUtils.writeLines(file, linesToWrite, false);
		}
		Main.logger.print((hasFileChanged ? "Changed" : "Unchanged") + ": " + file.getPath());
		return hasFileChanged;
	}
}
