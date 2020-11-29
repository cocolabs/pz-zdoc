package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.luadoc.Main;
import io.yooksi.pz.luadoc.element.JavaClass;
import io.yooksi.pz.luadoc.element.JavaMethod;
import io.yooksi.pz.luadoc.element.LuaMethod;
import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.ParseRegex;

/**
 * This class represents a parsed JavaDoc document.
 */
public class JavaDoc<L> extends CodeDoc<JavaMethod> {

	public static final String PZ_API_GLOBAL_URL =
			"https://projectzomboid.com/modding/zombie/Lua/LuaManager.GlobalObject.html";

	public JavaDoc(String name, Set<JavaClass<L>> members, List<JavaMethod> methods) {
		super(name, new ArrayList<>(), members, methods);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, JavaClass<L>> getMembers() {
		return (Map<String, JavaClass<L>>) super.getMembers();
	}

	public LuaDoc convertToLuaDoc(boolean annotate, boolean qualify) {

		List<String> lines = new java.util.ArrayList<>();
		List<LuaMethod> luaMethods = new java.util.ArrayList<>();

		List<JavaMethod> javaMethods = getMethods();
		for (JavaMethod method : javaMethods)
		{
			LuaMethod luaMethod = !qualify ? LuaMethod.Parser.create(method).parse()
					: LuaMethod.Parser.create(method, getName()).parse();

			if (annotate) {
				luaMethod.annotate();
			}
			luaMethods.add(luaMethod);
			lines.add(luaMethod.toString());
			lines.add("");
		}
		lines.remove(lines.size() - 1);
		return new LuaDoc(getName(), lines, new java.util.HashSet<>(), luaMethods);
	}

	public static abstract class Parser<T> extends DataParser<JavaDoc<T>, T> {

		protected final Document document;

		protected Parser(T data, Document document) {
			super(data);
			this.document = document;
		}

		public static String removeElementQualifier(String element) {
			return element.replaceAll(".\\w+\\.", "");
		}

		protected Element getMethodSummary() throws NoSuchElementException {

			return Objects.requireNonNull(document).select("table").stream()
					.filter(t -> t.className().equals("memberSummary") &&
							t.attr("summary").startsWith("Method Summary")).findFirst()
					.orElseThrow(() -> new NoSuchElementException("Unable to find method summary table."));
		}

		protected Element getMethodDetails() {

			return Objects.requireNonNull(document).getElementsByTag("a").stream()
					.filter(e -> e.hasAttr("name") && e.attr("name")
							.equals("method.detail")).findFirst().orElseThrow(() ->
							new NoSuchElementException("Unable to find method details table")).parent();
		}

		protected String parseMethodAccessModifier(String methodName, int index) {

			Elements methodDetails = getMethodDetails().getElementsByTag("ul");
			for (int i = 0; i < methodDetails.size(); i++)
			{
				Element element = methodDetails.get(i);
				Elements headers = element.getElementsByTag("h4");
				if (!headers.isEmpty() && headers.first().text().equals(methodName) && i == index)
				{
					Elements preTags = element.getElementsByTag("pre");
					if (!preTags.isEmpty())
					{
						Matcher match = ParseRegex.FIRST_WORD_REGEX.matcher(preTags.first().text());
						if (match.find()) {
							return match.group(1);
						}
					}
					Main.LOGGER.warn(String.format("Expected to find method access " +
							"modifier (i=%d) for method %s", i, methodName));
				}
			}
			Main.LOGGER.error("Unable to parse method access modifier for method " + methodName);
			return "public";
		}

		protected List<JavaMethod> parseMethods(Element methodSummary) {

			Elements tableRows = methodSummary.getElementsByTag("tr");

			// remove table header
			tableRows.remove(0);

			List<JavaMethod> methods = new ArrayList<>();

			for (int i = 0; i < tableRows.size(); i++)
			{
				Element element = tableRows.get(i);

				Elements columns = element.getElementsByTag("td");
				Element colFirst = columns.first();
				Element colLast = columns.last();

				String methodName = colLast.getElementsByClass("memberNameLink").text();
				if (parseMethodAccessModifier(methodName, i).equals("public"))
				{
					String methodText = colFirst.text() + " " + colLast.text();

					JavaMethod javaMethod = JavaMethod.Parser.create(methodText).parse();
					/* Do not add methods which were not parsed */
					if (javaMethod != null) {
						methods.add(javaMethod);
					}
				}
				else Main.LOGGER.warn("Skipping non-public method " + methodName);
			}
			return methods;
		}

		protected List<Element> parseMemberHyperlinks(Element methodSummary) {

			return methodSummary.getElementsByTag("a").stream()
					.filter(e -> e.attr("title").startsWith("class in"))
					.collect(Collectors.toList());
		}
	}

	public static class WebParser extends Parser<URL> {

		private WebParser(URL data) throws IOException {
			super(data, Jsoup.connect(data.toString()).get());
		}

		/**
		 * @throws IOException if Jsoup failed connecting to or parsing document.
		 * @throws MalformedURLException if url string cannot be converted to URL object.
		 */
		public static WebParser create(String url) throws IOException {
			return new WebParser(new URL(url));
		}

		@Override
		public JavaDoc<URL> parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Element summaryTable = getMethodSummary();
			List<JavaMethod> methods = parseMethods(summaryTable);

			Set<JavaClass<URL>> members = new java.util.HashSet<>();
			for (Element member : parseMemberHyperlinks(summaryTable))
			{
				try {
					URL url = new java.net.URL(member.attr("abs:href"));
					members.add(new JavaClass<>(member.text(), url));
				}
				catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			String filename = Paths.get(data.getPath()).getFileName().toString();
			return new JavaDoc<>(FilenameUtils.getBaseName(filename), members, methods);
		}

		public Path getRelativeFilePath() {
			return Paths.get("/modding/").relativize(Paths.get(data.getPath()));
		}

		public Path getOutputFilePath(String fileExtension) {

			Path pRelative = getRelativeFilePath();
			String baseFilename = FilenameUtils.getBaseName(pRelative.getFileName().toString());
			return pRelative.getParent().resolve(baseFilename + '.' + fileExtension);
		}
	}

	public static class FileParser extends Parser<Path> {

		private FileParser(Path data) throws IOException {
			super(data, Jsoup.parse(data.toFile(), Charset.defaultCharset().name()));
		}

		/**
		 * @throws IOException if the file could not be found or read.
		 * @throws InvalidPathException if the path string cannot be converted to a {@code Path}.
		 */
		public static FileParser create(String path) throws IOException {
			return new FileParser(Paths.get(path));
		}

		@Override
		public JavaDoc<Path> parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Element summaryTable = getMethodSummary();
			List<JavaMethod> methods = parseMethods(summaryTable);

			Set<JavaClass<Path>> members = new java.util.HashSet<>();
			for (Element member : parseMemberHyperlinks(summaryTable))
			{
				try {
					Path path = Paths.get(member.attr("title").substring(9));
					members.add(new JavaClass<>(member.text(), path));
				}
				catch (InvalidPathException e) {
					throw new RuntimeException(e);
				}
			}
			return new JavaDoc<>(data.getFileName().toString(), members, methods);
		}
	}
}
