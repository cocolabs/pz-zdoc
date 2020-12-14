/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import io.yooksi.pz.zdoc.cmd.Command;
import io.yooksi.pz.zdoc.cmd.CommandLine;
import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.doc.LuaDoc;
import io.yooksi.pz.zdoc.element.JavaClass;
import io.yooksi.pz.zdoc.element.LuaClass;
import io.yooksi.pz.zdoc.element.Method;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.parser.JavaDocFileParser;
import io.yooksi.pz.zdoc.parser.JavaDocWebParser;
import io.yooksi.pz.zdoc.parser.LuaDocParser;
import zombie.Lua.LuaManager;
import zombie.core.Core;

public class Main {

	/**
	 * <p>Application main entry point method.</p>
	 * <p>Supports the following command forms:</p>
	 * <ul>
	 *     <li>
	 *         <i>Annotate existing lua files under given path:</i>
	 *         <pre>{@code -lua <path_to_files> <output_dir_path>}</pre>
	 *     </li>
	 *     <li>
	 *         <i>Parse java doc under given <i>path/url</i> and convert to lua file:</i>
	 *         <pre>{@code -java [--api|<java_doc_location>] <output_dir_path>}</pre>
	 *     </li>
	 * </ul>
	 *
	 * @throws InvalidPathException if malformed path passed as argument
	 * @throws NoSuchFileException if unable to find file under argument path
	 * @throws IndexOutOfBoundsException if no path argument supplied
	 */
	public static void main(String[] args) throws IOException, ParseException {

		Logger.debug(String.format("Started application with %d args: %s",
				args.length, Arrays.toString(args)));

		Command command = Command.parse(args);
		if (command == null) {
			throw new ParseException("Missing or unknown command argument");
		}
		else if (command == Command.HELP)
		{
			Command info = Command.parse(args, 1);
			if (info == null) {
				CommandLine.printHelp(Command.values());
			}
			else CommandLine.printHelp(info);
			return;
		}
		CommandLine cmdLine = CommandLine.parse(command.getOptions(), args);
		// parse and document LUA files
		if (command == Command.ANNOTATE)
		{
			Logger.debug("Preparing to parse and document lua files...");

			Path root = cmdLine.getInputPath();
			Path dir = cmdLine.getOutputPath();
			java.util.List<Path> paths = Files.walk(Paths.get(root.toString()))
					.filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new));

			if (paths.size() > 1) {
				Logger.info("Parsing and documenting lua files found in " + root);
			}
			else if (paths.isEmpty()) {
				Logger.warn("No files found under path " + root);
			}
			// process every file found under given root path
			Set<String> excludedMembers = new java.util.HashSet<>();
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
					if (!outputFile.exists())
					{
						File parentFile = outputFile.getParentFile();
						if (!parentFile.exists() && (!parentFile.mkdirs() || !outputFile.createNewFile())) {
							throw new IOException("Unable to create specified output file: " + outputFilePath);
						}
					}
					LuaDoc doc = LuaDocParser.create(path.toFile(), excludedMembers).parse();
					if (!doc.getMembers().isEmpty()) {
						doc.writeToFile(outputFilePath);
					}
				}
			}
		}
		// parse JAVA docs and document LUA files
		else if (command == Command.COMPILE)
		{
			Logger.debug("Preparing to parse java doc...");

			Path userOutput = cmdLine.getOutputPath();
			if (userOutput == null)
			{
				userOutput = Paths.get(".");
				Logger.debug("Output directory not specified, using root directory instead");
			}
			else Logger.debug("Output directory set to " + userOutput.toString());

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

			if (cmdLine.isInputApi())
			{
				Object source = cmdLine.getApiLocation();
				if (source == null) {
					source = JavaDoc.API_GLOBAL_OBJECT;
				}
				if (source instanceof URL)
				{
					Logger.debug("Reading from api URL: " + source.toString());

					boolean includeRefs = cmdLine.shouldIncludeRefs();
					List<Method> methods = new ArrayList<>();

					List<String> userExclude = cmdLine.getExcludedClasses();
					Set<String> exclude = new HashSet<>(userExclude);

					JavaDocWebParser parser = JavaDocWebParser.create((URL) source);
					JavaDoc<URL> javaDoc = parser.parse();

					Path output = userOutput.resolve(parser.getOutputFilePath("lua"));
					LuaDoc luaDoc = javaDoc.compileLuaLibrary(true, false);
					exclude.add(luaDoc.getName());

					if (includeRefs) {
						methods.addAll(luaDoc.getMethods());
					}
					luaDoc.writeToFile(output);

					for (Map.Entry<String, JavaClass<URL>> entry : javaDoc.getMembers().entrySet())
					{
						JavaClass<URL> javaClass = entry.getValue();
						if (userExclude.contains(javaClass.getName())) {
							continue;
						}
						String memberUrl = javaClass.getLocation().toString();
						JavaDocWebParser memberParser = JavaDocWebParser.create(memberUrl);

						output = userOutput.resolve(memberParser.getOutputFilePath("lua"));
						luaDoc = memberParser.parse().compileLuaLibrary(true, true);
						exclude.add(luaDoc.getName());

						if (includeRefs) {
							methods.addAll(luaDoc.getMethods());
						}
						luaDoc.writeToFile(output);
					}
					if (includeRefs)
					{
						File membersFile = userOutput.resolve("Members.lua").toFile();
						if (!membersFile.exists() && !membersFile.createNewFile()) {
							throw new IOException("Unable to create Members.lua");
						}
						List<String> memberDoc = LuaClass.documentRefMembers(methods, exclude);
						FileUtils.writeLines(membersFile, memberDoc, false);
					}
				}
				else {
					Path sourcePath = (Path) source;
					JavaDocFileParser parser = JavaDocFileParser.create(sourcePath);
					Path output = userOutput.resolve(sourcePath.getFileName());
					parser.parse().compileLuaLibrary(true, false).writeToFile(output);
				}
			}
		}
		Logger.debug("Finished processing command");
	}

	/**
	 * Initialize {@link LuaManager} and return a set of exposed Java classes.
	 *
	 * @param debug should debug related classes be exposed.
	 * @return a set of exposed Java classes.
	 *
	 * @throws IllegalAccessException if the private field ({@code LuaManager.Exposer#exposed})
	 * 		holding the set of exposed Java classes could not be found.
	 */
	@SuppressWarnings("unchecked")
	static HashSet<Class<?>> getExposedJava(boolean debug) throws IllegalAccessException {

		Core.bDebug = debug;
		try {
			Logger.debug("Initializing PZ LuaManager...");
			LuaManager.init();
		}
		catch (Throwable e) {
			Logger.debug(String.format("Caught exception %s: %s", e.getClass().getName(), e.getMessage()));
		}
		return (HashSet<Class<?>>) FieldUtils.readDeclaredField(
				LuaManager.exposer, "exposed", true);
	}
}
