package io.yooksi.pz.luadoc.doc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.luadoc.element.JavaMethod;
import io.yooksi.pz.luadoc.element.LuaMethod;
import io.yooksi.pz.luadoc.element.MemberClass;
import io.yooksi.pz.luadoc.element.Method;
import io.yooksi.pz.luadoc.lang.DataParser;

public class JavaDoc extends CodeDoc<JavaMethod> {

	public static final String PZ_API_URL = "https://projectzomboid.com/modding/";
	public static final String PZ_API_GLOBAL_URL = PZ_API_URL + "Lua/LuaManager.GlobalObject.html";

	public JavaDoc(List<String> content, List<MemberClass> members, List<JavaMethod> methods) {
		super(content, members, methods);
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
		return new LuaDoc(lines, new ArrayList<>(), luaMethods);
	}

	public static class Parser extends DataParser<JavaDoc, Document> {

		public static String removeElementQualifier(String element) {
			return element.replaceAll(".\\w+\\.", "");
		}

		public Parser loadURL(String url) throws IOException {
			return (Parser) input(Jsoup.connect(url).get());
		}

		public Parser loadFile(String path) throws IOException {
			return (Parser) input(Jsoup.parse(new File(path), Charset.defaultCharset().name()));
		}

		@Override
		public JavaDoc parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Elements tables = data.select("table");
			java.util.Set<Element> summaryTables = tables.stream()
					.filter(t -> t.className().equals("memberSummary"))
					.collect(Collectors.toSet());

			Element methodTable = summaryTables.stream()
					.filter(t -> t.attr("summary").startsWith("Method Summary"))
					.collect(Collectors.toSet()).toArray(new Element[]{})[0];

			Elements tableRows = methodTable.getElementsByTag("tr");

			// remove table header
			tableRows.remove(0);

			List<JavaMethod> methods = new ArrayList<>();
			for (Element element : tableRows)
			{
				Elements columns = element.getElementsByTag("td");
				String methodText = columns.first().text() + " " + columns.last().text();

				methods.add(Method.JAVA_PARSER.input(methodText).parse());
			}
			return new JavaDoc(null, new ArrayList<>(), methods);
		}
	}
}
