package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.luadoc.element.JavaClass;
import io.yooksi.pz.luadoc.element.JavaMethod;
import io.yooksi.pz.luadoc.element.LuaMethod;
import io.yooksi.pz.luadoc.lang.DataParser;

/**
 * This class represents a parsed JavaDoc document.
 */
public class JavaDoc extends CodeDoc<JavaMethod> {

	public static final String PZ_API_URL = "https://projectzomboid.com/modding/";
	public static final String PZ_API_GLOBAL_URL = PZ_API_URL + "zombie/Lua/LuaManager.GlobalObject.html";

	public JavaDoc(Set<JavaClass<?>> members, List<JavaMethod> methods) {
		super(new ArrayList<>(), members, methods);
	}

	public LuaDoc convertToLuaDoc(boolean annotate) {

		List<String> lines = new java.util.ArrayList<>();
		List<LuaMethod> luaMethods = new java.util.ArrayList<>();

		List<JavaMethod> javaMethods = getMethods();
		for (JavaMethod method : javaMethods)
		{
			LuaMethod luaMethod = LuaMethod.Parser.create(method).parse();
			if (annotate) {
				luaMethod.annotate();
			}
			luaMethods.add(luaMethod);
			lines.add(luaMethod.toString());
			lines.add("");
		}
		lines.remove(lines.size() - 1);
		return new LuaDoc(lines, new java.util.HashSet<>(), luaMethods);
	}

	public static abstract class Parser<T> extends DataParser<JavaDoc, T> {

		protected final Document document;

		protected Parser(T data, Document document) {
			super(data); this.document = document;
		}

		public static String removeElementQualifier(String element) {
			return element.replaceAll(".\\w+\\.", "");
		}

		protected Element getMethodSummary() throws NoSuchElementException {

			return Objects.requireNonNull(document).select("table").stream()
					.filter(t -> t.className().equals("memberSummary") &&
							t.attr("summary").startsWith("Method Summary")).findFirst()
					.orElseThrow(() -> new NoSuchElementException("Unable to find method summary."));
		}

		protected Element getMethodDetails() {

			return Objects.requireNonNull(document).select("a").stream()
					.filter(e -> e.attr("name").equals("method.details"))
					.collect(Collectors.toSet()).toArray(new Element[]{})[0];
		}

		protected List<JavaMethod> parseMethods(Element methodSummary) {

			Elements tableRows = methodSummary.getElementsByTag("tr");

			// remove table header
			tableRows.remove(0);

			List<JavaMethod> methods = new ArrayList<>();
			for (Element element : tableRows)
			{
				Elements columns = element.getElementsByTag("td");
				String methodText = columns.first().text() + " " + columns.last().text();

				methods.add(JavaMethod.Parser.create(methodText).parse());
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
		public JavaDoc parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Element summaryTable = getMethodSummary();
			List<JavaMethod> methods = parseMethods(summaryTable);

			Set<JavaClass<?>> members = new java.util.HashSet<>();
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
			return new JavaDoc(members, methods);
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
		public JavaDoc parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Element summaryTable = getMethodSummary();
			List<JavaMethod> methods = parseMethods(summaryTable);

			Set<JavaClass<?>> members = new java.util.HashSet<>();
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
			return new JavaDoc(members, methods);
		}
	}
}
