package io.yooksi.pz.luadoc.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import io.yooksi.pz.luadoc.Main;
import io.yooksi.pz.luadoc.lang.EmmyLua;
import org.jetbrains.annotations.Nullable;

public class LuaParser {

	private static final Pattern DERIVED_CLASS = Pattern.compile("=\\s*(\\w+):derive\\(");

	public static boolean documentLuaFile(Path root, File file, @Nullable File outputDir) throws IOException {

		if (!root.toFile().exists()) {
			throw new FileNotFoundException(root.toString());
		}
		else if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		Path relativePath = root.relativize(file.toPath());
		File outputFile;
		if (outputDir != null) {
			if (!outputDir.exists() && !outputDir.mkdir()) {
				throw new IOException("Unable to create output directory: " + outputDir.getPath());
			}
			outputFile = outputDir.toPath().resolve(relativePath).toFile();
		}
		// overwrite file when unspecified output directory
		else outputFile = file;

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
					if (EmmyLua.CLASS.isAnnotation(prevLine)) {
						linesToWrite.remove(i - 1);
					}
				}
				String annotation = EmmyLua.CLASS.create(filename);
				Matcher matcher = DERIVED_CLASS.matcher(line);
				if (matcher.find()) {
					annotation += " : " + matcher.group(1);
				}
				linesToWrite.add(annotation);
				hasFileChanged = true;
			}
			linesToWrite.add(line);
		}
		if (hasFileChanged)
		{
			if (!outputFile.exists()) {
				File parentFile = outputFile.getParentFile();
				if (!parentFile.exists() && (!parentFile.mkdirs() || !outputFile.createNewFile())) {
					throw new IOException("Unable to create specified output file: " + outputFile.getPath());
				}
			}
			FileUtils.writeLines(outputFile, linesToWrite, false);
		}
		Main.logger.print((hasFileChanged ? "Changed" : "Unchanged") + ": " + file.getPath());
		return hasFileChanged;
	}

	public static boolean documentLuaFile(Path root, File file, @Nullable Path output) throws IOException {
		return documentLuaFile(root, file, output != null ? output.toFile() : null);
	}

	public static boolean documentLuaFile(File file, @Nullable File output) throws IOException {
		return documentLuaFile(file.getParentFile().toPath(), file, output);
	}

	public static boolean documentLuaFile(File file) throws IOException {
		return documentLuaFile(file.getParentFile().toPath(), file, (Path) null);
	}
}
