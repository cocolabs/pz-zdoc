/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.util.Strings;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import io.cocolabs.pz.zdoc.cmd.Command;
import io.cocolabs.pz.zdoc.cmd.CommandLine;
import io.cocolabs.pz.zdoc.compile.CompilerException;
import io.cocolabs.pz.zdoc.compile.JavaCompiler;
import io.cocolabs.pz.zdoc.compile.LuaAnnotator;
import io.cocolabs.pz.zdoc.compile.LuaCompiler;
import io.cocolabs.pz.zdoc.doc.ZomboidJavaDoc;
import io.cocolabs.pz.zdoc.doc.ZomboidLuaDoc;
import io.cocolabs.pz.zdoc.element.lua.LuaClass;
import io.cocolabs.pz.zdoc.logger.Logger;
import io.cocolabs.pz.zdoc.util.Utils;

public class Main {

	public static final String CHARSET = StandardCharsets.UTF_8.name();
	public static final ClassLoader CLASS_LOADER = Main.class.getClassLoader();
	static final Map<String, String> CLASS_OVERRIDES = new HashMap<>();

	public static void main(String[] args) throws IOException, ParseException, CompilerException {

		Logger.debug(String.format("Started application with %d args: %s",
				args.length, Arrays.toString(args)));

		Command command = Command.parse(args);
		if (command == null)
		{
			String format = "Missing or unknown command argument (%s)";
			throw new ParseException(String.format(format, Arrays.toString(args)));
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
				/* first try to find version file project root directory,
				 * available when we are not running from a jar
				 */
				String zdocVersion;
				File versionFile = new File("version.txt");
				if (!versionFile.exists())
				{
					try (InputStream iStream = CLASS_LOADER.getResourceAsStream("version.txt"))
					{
						if (iStream == null) {
							throw new FileNotFoundException("Unable to read version, missing version.txt");
						}
						try (Reader reader = new InputStreamReader(iStream, Charsets.UTF_8)) {
							zdocVersion = CharStreams.toString(reader);
						}
					}
				}
				else zdocVersion = FileUtils.readFileToString(versionFile, CHARSET);
				Logger.info("zdoc version " + zdocVersion);

				Class<?> coreClass = Utils.getClassForName("zombie.core.Core");
				Object core = MethodUtils.invokeStaticMethod(coreClass, "getInstance");

				Object gameVersion = MethodUtils.invokeExactMethod(core, "getGameVersion");
				Object sGameVersion = MethodUtils.invokeExactMethod(gameVersion, "toString");

				Logger.info("game version " + sGameVersion);
				return;
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
			List<Path> paths;
			Path dir = cmdLine.getOutputPath();
			try (Stream<Path> stream = Files.walk(Paths.get(root.toString()))) {
				paths = stream.filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new));
			}
			if (paths.size() > 1) {
				Logger.info("Parsing and documenting lua files found in " + root);
			}
			else if (paths.isEmpty()) {
				Logger.warn("No files found under path " + root);
			}
			boolean onlyAnnotated = cmdLine.includeOnlyAnnotated();
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
					File outputFile = outputFilePath.toFile();
					String fileName = path.getFileName().toString();
					List<String> content = new ArrayList<>();

					LuaAnnotator.AnnotateRules rules = new LuaAnnotator.AnnotateRules(properties, exclude);
					LuaAnnotator.AnnotateResult result = LuaAnnotator.annotate(path.toFile(), content, rules);

					String addendum = outputFile.exists() ? " and overwriting" : "";
					Logger.debug(String.format("Annotating%s file %s...", addendum, fileName));
					switch (result)
					{
						case ALL_INCLUDED:
							Logger.info(String.format("Finished annotating file \"%s\", " +
									"all elements matched.", fileName));
							writeAnnotatedLinesToFile(content, outputFile);
							break;
						case PARTIAL_INCLUSION:
							Logger.error(String.format("Failed annotating file \"%s\", " +
									"some elements were not matched.", fileName));
							writeAnnotatedLinesToFile(content, outputFile);
							break;
						case NO_MATCH:
							Logger.error(String.format("Failed annotating file \"%s\", " +
									"no elements were matched", fileName));
							if (!onlyAnnotated) {
								writeAnnotatedLinesToFile(content, outputFile);
							}
							break;
						case SKIPPED_FILE_IGNORED:
							Logger.warn(String.format("Skipped annotating file \"%s\", " +
									"file was ignored.", fileName));
							if (!onlyAnnotated) {
								writeAnnotatedLinesToFile(content, outputFile);
							}
							break;
						case SKIPPED_FILE_EMPTY:
							Logger.warn(String.format("Skipped annotating file \"%s\", " +
									"file was empty.", fileName));
							if (!onlyAnnotated) {
								writeAnnotatedLinesToFile(content, outputFile);
							}
							break;
						case ALL_EXCLUDED:
							Logger.warn(String.format("Skipped annotating file \"%s\", " +
									"all elements were excluded.", fileName));
							if (!onlyAnnotated) {
								writeAnnotatedLinesToFile(content, outputFile);
							}
							break;
					}
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
			Logger.debug("Reading compile.properties, found %d keys", properties.size());

			String excludeProp = properties.getProperty("exclude");
			if (!StringUtils.isBlank(excludeProp))
			{
				List<String> excludeEntries = Arrays.asList(excludeProp.split(","));
				Logger.debug("Loaded %d exclude entries from compile.properties", excludeEntries.size());
				exclude.addAll(excludeEntries);
			}
			// remove exclude property so it doesnt get included as override entry
			properties.remove(exclude);

			// store all class override entries in map
			for (Map.Entry<Object, Object> entry : properties.entrySet())
			{
				String override = (String) entry.getValue();
				if (Strings.isNotBlank(override)) {
					CLASS_OVERRIDES.put((String) entry.getKey(), override);
				}
			}
			Set<ZomboidJavaDoc> compiledJava = new JavaCompiler(exclude).compile();
			Set<ZomboidLuaDoc> compiledLua = new LuaCompiler(compiledJava).compile();
			for (ZomboidLuaDoc zLuaDoc : compiledLua)
			{
				String luaDocName = zLuaDoc.getName();
				String luaDocProp = properties.getProperty(luaDocName);
				if (luaDocProp != null)
				{
					// override lua document name with property value
					if (!StringUtils.isBlank(luaDocProp))
					{
						ZomboidLuaDoc overrideDoc = new ZomboidLuaDoc(
								new LuaClass(luaDocProp, zLuaDoc.getClazz().getParentType()),
								zLuaDoc.getFields(), zLuaDoc.getMethods()
						);
						overrideDoc.writeToFile(userOutput.resolve(luaDocProp + ".lua").toFile());
					}
				}
				else zLuaDoc.writeToFile(userOutput.resolve(luaDocName + ".lua").toFile());
			}
			Logger.info("Compiled and written %d lua documents", compiledLua.size());
			ZomboidLuaDoc.writeGlobalTypesToFile(userOutput.resolve("Types.lua").toFile());
			for (String excludedClass : exclude) {
				Logger.warn("Class " + excludedClass + " was designated but not excluded from compilation.");
			}
		}
		Logger.debug("Finished processing command");
		/*
		 * exit application gracefully to avoid unpredictable
		 * AGENT_ERROR_NO_JNI_ENV error that is present in J8 sun classes:
		 * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6476706
		 */
		System.exit(0);
	}

	/**
	 * <p>Returns Lua class name that is safe to use in compiled library.</p>
	 * <p>Note that it is safe to pass array class names (ex. {@code Vector2[]}).</p>
	 *
	 * @param name name of the class name to lookup.
	 */
	public static String getSafeLuaClassName(String name) {

		boolean isArray = name.endsWith("[]");
		String sName = isArray ? name.substring(0, name.length() - 2) : name;

		String override = CLASS_OVERRIDES.get(sName);
		return override != null ? (!isArray ? override : override + "[]") : name;
	}

	private static void writeAnnotatedLinesToFile(List<String> lines, File file) throws IOException {

		// do not write empty content
		if (lines.isEmpty()) {
			return;
		}
		// make sure output file exists before we try to write to it
		if (!file.exists())
		{
			File parentFile = file.getParentFile();
			if (!parentFile.exists() && (!parentFile.mkdirs() || !file.createNewFile())) {
				throw new IOException("Unable to create specified output file: " + file);
			}
		}
		FileUtils.writeLines(file, lines, false);
	}
}
