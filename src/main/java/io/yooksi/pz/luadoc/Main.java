package io.yooksi.pz.luadoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.yooksi.pz.luadoc.cmd.Command;
import io.yooksi.pz.luadoc.cmd.CommandLine;
import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.doc.LuaDoc;
import io.yooksi.pz.luadoc.element.JavaClass;
import io.yooksi.pz.luadoc.element.LuaClass;
import io.yooksi.pz.luadoc.element.Method;

public class Main {

	public static final Logger LOGGER = LogManager.getLogger(Main.class);

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

		LOGGER.debug(String.format("Started application with %d args: %s",
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
		if (command == Command.LUA)
		{
			LOGGER.debug("Preparing to parse and document lua files...");

			Path root = cmdLine.getInputPath();
			Path dir = cmdLine.getOutputPath();
			java.util.List<Path> paths = Files.walk(Paths.get(root.toString()))
					.filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new));

			if (paths.size() > 1) {
				LOGGER.info("Parsing and documenting lua files found in " + root);
			}
			else if (paths.isEmpty()) {
				LOGGER.warn("No files found under path " + root);
			}
			// process every file found under given root path
			Set<String> excludedMembers = new java.util.HashSet<>();
			for (Path path : paths)
			{
				if (Utils.isLuaFile(path))
				{
					LOGGER.debug(String.format("Found lua file \"%s\"", path.getFileName()));
					Path outputFilePath = validateLuaOutputPath(path, root, dir);

					LuaDoc doc = LuaDoc.Parser.create(path.toFile(), excludedMembers).parse();
					if (!doc.getMembers().isEmpty()) {
						doc.writeToFile(outputFilePath);
					}
				}
			}
		}
		// parse JAVA docs and document LUA files
		else if (command == Command.JAVA)
		{
			LOGGER.debug("Preparing to parse java doc...");

			Object source;
			if (cmdLine.isInputApi())
			{
				LOGGER.debug("Reading from online api");
				source = cmdLine.getInputUrl();
				if (source == null) {
					source = JavaDoc.API_GLOBAL_OBJECT;
				}
			}
			else source = cmdLine.getInputPath();
			LOGGER.debug("Reading from source " + source);

			Path userOutput = cmdLine.getOutputPath();
			if (userOutput == null)
			{
				userOutput = Paths.get(".");
				LOGGER.debug("Output directory not specified, using root directory instead");
			}
			else LOGGER.debug("Output directory set to " + userOutput.toString());

			File outputDir = userOutput.toFile();
			if (!outputDir.exists())
			{
				if (!outputDir.mkdirs()) {
					throw new IOException("Unable to create output directory");
				}
			} else if (!outputDir.isDirectory()) {
				throw new IllegalArgumentException("Output path does not point to a directory");
			}
			else LOGGER.debug("Designated output path: " + userOutput);

			if (source instanceof URL)
			{
				Set<String> exclude = new HashSet<>();
				JavaDoc.WebParser parser = JavaDoc.WebParser.create((URL) source);
				JavaDoc<URL> javaDoc = parser.parse();

				Path output = userOutput.resolve(parser.getOutputFilePath("lua"));
				LuaDoc luaDoc = javaDoc.convertToLuaDoc(true, false);
				exclude.add(luaDoc.getName());

				List<Method> methods = new ArrayList<>(luaDoc.getMethods());
				luaDoc.writeToFile(output);

				for (Map.Entry<String, JavaClass<URL>> entry : javaDoc.getMembers().entrySet())
				{
					String memberUrl = entry.getValue().getLocation().toString();
					JavaDoc.WebParser memberParser = JavaDoc.WebParser.create(memberUrl);

					output = userOutput.resolve(memberParser.getOutputFilePath("lua"));
					luaDoc = memberParser.parse().convertToLuaDoc(true, true);
					exclude.add(luaDoc.getName());

					methods.addAll(luaDoc.getMethods());
					luaDoc.writeToFile(output);
				}
				File membersFile = userOutput.resolve("Members.lua").toFile();
				if (!membersFile.exists() && !membersFile.createNewFile()) {
					throw new IOException("Unable to create Members.lua");
				}
				List<String> memberDoc = LuaClass.documentMembers(methods, exclude);
				FileUtils.writeLines(membersFile, memberDoc, false);
			}
			else if (source != null)
			{
				JavaDoc.FileParser parser = JavaDoc.FileParser.create((Path) source);
				Path output = userOutput.resolve(((Path) source).getFileName());
				parser.parse().convertToLuaDoc(true, false).writeToFile(output);
			}
			else throw new IllegalArgumentException("Unable to parse input path/url");
		}
		LOGGER.debug("Finished processing command");
	}

	private static Path validateLuaOutputPath(Path path, Path root, Path dir) throws IOException {

		Path outputPath;
		if (!root.toFile().exists()) {
			throw new FileNotFoundException(root.toString());
		}
		/* user did not specify output dir path */
		else if (dir != null)
		{
			File outputDirFile = dir.toFile();
			if (!outputDirFile.exists() && !outputDirFile.mkdir()) {
				throw new IOException("Unable to create output directory: " + dir);
			}
			/* root path matches current path so there are no
			 * subdirectories, just resolve the filename against root path
			 */
			if (root.compareTo(path) == 0) {
				outputPath = dir.resolve(path.getFileName());
			}
			else outputPath = dir.resolve(root.relativize(path));
		}
		/* overwrite file when unspecified output directory */
		else
		{
			outputPath = path;
			Main.LOGGER.warn("Unspecified output directory, overwriting files");
		}
		/* make sure output file exists before we try to write to it */
		File outputFile = outputPath.toFile();
		if (!outputFile.exists())
		{
			File parentFile = outputFile.getParentFile();
			if (!parentFile.exists() && (!parentFile.mkdirs() || !outputFile.createNewFile())) {
				throw new IOException("Unable to create specified output file: " + outputPath);
			}
		}
		return outputPath;
	}
}
