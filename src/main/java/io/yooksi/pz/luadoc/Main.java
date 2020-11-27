package io.yooksi.pz.luadoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.doc.LuaDoc;

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
	 *         <pre>{@code -java <path_to_files> <output_dir_path>}</pre>
	 *     </li>
	 * </ul>
	 *
	 * @throws InvalidPathException if malformed path passed as argument
	 * @throws NoSuchFileException if unable to find file under argument path
	 * @throws IndexOutOfBoundsException if no path argument supplied
	 */
	public static void main(String[] args) throws IOException {

		LOGGER.debug(String.format("Started application with %d args: %s",
				args.length, Arrays.toString(args)));

		if (args.length == 0) {
			throw new IllegalArgumentException("No application argument supplied");
		}
		String rawOpArg = args[0];
		/*
		 * validate application argument format
		 */
		Matcher matcher = Pattern.compile("^\\s*+-(\\w+)\\s*$").matcher(rawOpArg);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Malformed application argument: " + rawOpArg);
		}
		// application operation argument
		String opArg = matcher.group(1);

		// parse and document LUA files
		if (opArg.equals("lua"))
		{
			LOGGER.debug("Preparing to parse and document lua files...");
			Path rootPath, outputDir;
			java.util.List<Path> paths;
			try {
				rootPath = Paths.get(args[1]);
				outputDir = args.length >= 3 ? Paths.get(args[2]) : null;
				paths = Files.walk(Paths.get(rootPath.toString())).filter(Files::isRegularFile)
						.collect(Collectors.toCollection(ArrayList::new));
			}
			catch (IndexOutOfBoundsException e) {
				throw new RuntimeException("No file path supplied", e);
			}
			if (paths.size() > 1) {
				LOGGER.info("Parsing and documenting lua files found in " + rootPath);
			}
			else if (paths.isEmpty()) {
				LOGGER.warn("No files found under path " + rootPath);
			}
			// process every file found under given root path
			for (Path path : paths)
			{
				if (Utils.isLuaFile(path))
				{
					LOGGER.debug(String.format("Found lua file \"%s\"", path.getFileName()));
					Path outputFilePath;

					if (!rootPath.toFile().exists()) {
						throw new FileNotFoundException(rootPath.toString());
					}
					/* user did not specify output dir path */
					else if (outputDir != null)
					{
						File outputDirFile = outputDir.toFile();
						if (!outputDirFile.exists() && !outputDirFile.mkdir()) {
							throw new IOException("Unable to create output directory: " + outputDir);
						}
						/* root path matches current path so there are no
						 * subdirectories, just resolve the filename against root path
						 */
						if (rootPath.compareTo(path) == 0) {
							outputFilePath = outputDir.resolve(path.getFileName());
						}
						else outputFilePath = outputDir.resolve(rootPath.relativize(path));
					}
					/* overwrite file when unspecified output directory */
					else
					{
						outputFilePath = path;
						LOGGER.warn("Unspecified output directory, overwriting files");
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
					new LuaDoc.Parser().input(path.toFile()).parse().writeToFile(outputFilePath);
				}
			}
		}
		// parse JAVA docs and document LUA files
		else if (opArg.equals("java"))
		{
			LOGGER.debug("Preparing to parse java doc...");
			Path output;
			try {
				output = Paths.get(args[1]);
				if (!output.toFile().exists()) {
					throw new NoSuchFileException(output.toString(), null, "Output file not found");
				}
				else LOGGER.debug("Designated output path: " + output);
			}
			catch (IndexOutOfBoundsException e) {
				throw new RuntimeException("No output file path supplied", e);
			}
			/* when source path is unspecified use API url */
			String source = args.length >= 3 ? args[2] : JavaDoc.PZ_API_GLOBAL_URL;

			JavaDoc.Parser<?> parser;
			if (Utils.isValidUrl(source)) {
				parser = new JavaDoc.WebParser().input(new URL(source));
			}
			else if (Utils.isValidPath(source)) {
				parser = new JavaDoc.FileParser().input(Paths.get(source));
			}
			else throw new IllegalArgumentException("\"" + source + "\" is not a valid file path or URL");

			JavaDoc javaDoc = parser.parse();
			javaDoc.convertToLuaDoc(true).writeToFile(output);
		}
		else throw new IllegalArgumentException("Unknown application argument: " + opArg);
		LOGGER.debug("Finished processing command");
	}
}
