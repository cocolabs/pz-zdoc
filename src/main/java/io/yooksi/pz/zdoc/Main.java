/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2021 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc;

import static io.yooksi.pz.zdoc.compile.LuaAnnotator.AnnotateResult;
import static io.yooksi.pz.zdoc.compile.LuaAnnotator.AnnotateRules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import io.yooksi.pz.zdoc.cmd.Command;
import io.yooksi.pz.zdoc.cmd.CommandLine;
import io.yooksi.pz.zdoc.compile.CompilerException;
import io.yooksi.pz.zdoc.compile.JavaCompiler;
import io.yooksi.pz.zdoc.compile.LuaAnnotator;
import io.yooksi.pz.zdoc.compile.LuaCompiler;
import io.yooksi.pz.zdoc.doc.ZomboidJavaDoc;
import io.yooksi.pz.zdoc.doc.ZomboidLuaDoc;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.util.Utils;

public class Main {

	public static final String CHARSET = Charset.defaultCharset().name();
	public static final ClassLoader CLASS_LOADER = Main.class.getClassLoader();

	/**
	 * <p>Application main entry point method.</p>
	 * <p>Accepts {@link Command} enums as application argument</p>
	 */
	public static void main(String[] args) throws IOException, ParseException, CompilerException {

		Logger.debug(String.format("Started application with %d args: %s",
				args.length, Arrays.toString(args)));

		Command command = Command.parse(args);
		if (command == null) {
			throw new ParseException("Missing or unknown command argument");
		}
		else if (command == Command.HELP)
		{
			Command info = args.length > 1 ? Command.parse(args, 1) : null;
			if (info == null) {
				CommandLine.printHelp(Command.values());
			}
			else CommandLine.printHelp(info);
			return;
		}
		else if (command == Command.VERSION)
		{
			try {
				Class<?> coreClass = Utils.getClassForName("zombie.core.Core");
				Object core = MethodUtils.invokeStaticMethod(coreClass, "getInstance");

				Object gameVersion = MethodUtils.invokeExactMethod(core, "getGameVersion");
				Object sGameVersion = MethodUtils.invokeExactMethod(gameVersion, "toString");

				Logger.info("game version " + sGameVersion);
			}
			catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
		CommandLine cmdLine = CommandLine.parse(command.getOptions(), args);
		Set<String> exclude = cmdLine.getExcludedClasses();
		if (command == Command.ANNOTATE)
		{
			Logger.debug("Preparing to parse and document lua files...");

			Path root = cmdLine.getInputPath();
			Path dir = cmdLine.getOutputPath();
			List<Path> paths = Files.walk(Paths.get(root.toString()))
					.filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new));

			if (paths.size() > 1) {
				Logger.info("Parsing and documenting lua files found in " + root);
			}
			else if (paths.isEmpty()) {
				Logger.warn("No files found under path " + root);
			}
			Properties properties = Utils.getProperties("annotate.properties");
			// process every file found under given root path
			for (Path path : paths)
			{
				if (Utils.isLuaFile(path))
				{
					Logger.debug(String.format("Found lua file \"%s\"", path.getFileName()));
					Path outputFilePath;
					if (!root.toFile().exists()) {
						throw new FileNotFoundException(root.toString());
					}
					/* user did not specify output dir path */
					else if (dir != null)
					{
						File outputDirFile = dir.toFile();
						if (!outputDirFile.exists() && !outputDirFile.mkdirs()) {
							throw new IOException("Unable to create output directory: " + dir);
						}
						/* root path matches current path so there are no
						 * subdirectories, just resolve the filename against root path
						 */
						if (root.compareTo(path) == 0) {
							outputFilePath = dir.resolve(path.getFileName());
						}
						else outputFilePath = dir.resolve(root.relativize(path));
					}
					/* overwrite file when unspecified output directory */
					else
					{
						outputFilePath = path;
						Logger.warn("Unspecified output directory, overwriting files");
					}
					/* make sure output file exists before we try to write to it */
					File outputFile = outputFilePath.toFile();
					boolean outputFileExists = outputFile.exists();
					if (!outputFileExists)
					{
						File parentFile = outputFile.getParentFile();
						if (!parentFile.exists() && (!parentFile.mkdirs() || !outputFile.createNewFile())) {
							throw new IOException("Unable to create specified output file: " + outputFilePath);
						}
					}
					String fileName = path.getFileName().toString();
					List<String> content = new ArrayList<>();

					AnnotateRules rules = new AnnotateRules(properties, exclude);
					AnnotateResult result = LuaAnnotator.annotate(path.toFile(), content, rules);

					String addendum = outputFileExists ? " and overwriting" : "";
					Logger.debug(String.format("Annotating%s file %s...", addendum, fileName));
					switch (result)
					{
						case ALL_INCLUDED:
							Logger.info(String.format("Finished annotating file \"%s\", " +
									"all elements matched.", fileName));
							break;
						case PARTIAL_INCLUSION:
							Logger.error(String.format("Failed annotating file \"%s\", " +
									"some elements were not matched.", fileName));
							break;
						case NO_MATCH:
							Logger.error(String.format("Failed annotating file \"%s\", " +
									"no elements were matched", fileName));
							break;
						case SKIPPED_FILE_IGNORED:
							Logger.info(String.format("Skipped annotating file \"%s\", " +
									"file was ignored.", fileName));
							break;
						case SKIPPED_FILE_EMPTY:
							Logger.warn(String.format("Skipped annotating file \"%s\", " +
									"file was empty.", fileName));
							break;
						case ALL_EXCLUDED:
							Logger.warn(String.format("Skipped annotating file \"%s\", " +
									"all elements were excluded.", fileName));
							break;
					}
					FileUtils.writeLines(outputFile, content, false);
				}
			}
		}
		else if (command == Command.COMPILE)
		{
			Logger.debug("Preparing to parse java doc...");

			Path userOutput = cmdLine.getOutputPath();
			if (userOutput == null)
			{
				userOutput = Paths.get(".");
				Logger.debug("Output directory not specified, using root directory instead");
			}
			File outputDir = userOutput.toFile();
			if (!outputDir.exists())
			{
				if (!outputDir.mkdirs()) {
					throw new IOException("Unable to create output directory");
				}
			}
			else if (!outputDir.isDirectory()) {
				throw new IllegalArgumentException("Output path does not point to a directory");
			}
			else Logger.debug("Designated output path: " + userOutput);

			Properties properties = Utils.getProperties("compile.properties");
			String excludeProp = properties.getProperty("exclude");
			if (excludeProp != null)
			{
				if (!StringUtils.isBlank(excludeProp)) {
					exclude.addAll(Arrays.asList(excludeProp.split(",")));
				}
			}
			Set<ZomboidJavaDoc> compiledJava = new JavaCompiler(exclude).compile();
			for (ZomboidLuaDoc zLuaDoc : new LuaCompiler(compiledJava).compile()) {
				zLuaDoc.writeToFile(userOutput.resolve(zLuaDoc.getName() + ".lua").toFile());
			}
			ZomboidLuaDoc.writeGlobalTypesToFile(userOutput.resolve("Types.lua").toFile());
			for (String excludedClass : exclude) {
				Logger.warn("Class " + excludedClass + " was designated but not excluded from compilation.");
			}
		}
		Logger.debug("Finished processing command");
	}
}
