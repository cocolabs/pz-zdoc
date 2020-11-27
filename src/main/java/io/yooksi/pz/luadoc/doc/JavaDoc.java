package io.yooksi.pz.luadoc.doc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
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
import io.yooksi.pz.luadoc.element.Method;
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
			LuaMethod luaMethod = Method.LUA_PARSER.input(method).parse();
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

	public static class Parser extends DataParser<JavaDoc, Document> {

		/** {@code true} if the document is being parsed from local disc. */
		private boolean isLocalDoc;

		public static String removeElementQualifier(String element) {
			return element.replaceAll(".\\w+\\.", "");
		}

		public Parser loadURL(String url) throws IOException {
			return (Parser) input(Jsoup.connect(url).get());
		}

		public Parser loadFile(String path) throws IOException {

			isLocalDoc = true;
			return (Parser) input(Jsoup.parse(new File(path), Charset.defaultCharset().name()));
		}

		private Element getMethodSummary() throws NoSuchElementException {

			return Objects.requireNonNull(data).select("table").stream()
					.filter(t -> t.className().equals("memberSummary") &&
							t.attr("summary").startsWith("Method Summary"))
					.findFirst().orElseThrow(() -> new NoSuchElementException("Unable to find method " +
							"summary" +
							"."));
		}

		private Element getMethodDetails() {

			return Objects.requireNonNull(data).select("a").stream()
					.filter(e -> e.attr("name").equals("method.details"))
					.collect(Collectors.toSet()).toArray(new Element[]{})[0];
		}

		@Override
		public JavaDoc parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Element summaryTable = getMethodSummary();
			Elements tableRows = summaryTable.getElementsByTag("tr");

			// remove table header
			tableRows.remove(0);

			List<JavaMethod> methods = new ArrayList<>();
			for (Element element : tableRows)
			{
				Elements columns = element.getElementsByTag("td");
				String methodText = columns.first().text() + " " + columns.last().text();

				methods.add(Method.JAVA_PARSER.input(methodText).parse());
			}
			Set<JavaClass<?>> members = new java.util.HashSet<>();
			List<Element> memberLinks = summaryTable.getElementsByTag("a").stream()
					.filter(e -> e.attr("title").startsWith("class in"))
					.collect(Collectors.toList());

			for (Element member : memberLinks)
			{
				try {
					members.add(new JavaClass<>(member.text(), !isLocalDoc ?
							new java.net.URL(member.attr("abs:href")) :
							Paths.get(member.attr("title").substring(9))));
				}
				catch (MalformedURLException | InvalidPathException e) {
					throw new RuntimeException(e);
				}
			}
			return new JavaDoc(members, methods);
		}
	}
}
