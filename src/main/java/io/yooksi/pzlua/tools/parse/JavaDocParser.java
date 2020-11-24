package io.yooksi.pzlua.tools.parse;

import io.yooksi.pzlua.tools.lang.ElementParser;
import io.yooksi.pzlua.tools.method.JavaMethod;
import io.yooksi.pzlua.tools.method.LuaFunction;
import io.yooksi.pzlua.tools.method.Method;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaDocParser {

	public static final String PZ_API_URL = "https://projectzomboid.com/modding/";
	public static final String PZ_API_GLOBAL_URL = PZ_API_URL + "Lua/LuaManager.GlobalObject.html";

	public static final ElementParser<JavaMethod> JAVA_METHOD_PARSER = new JavaMethod.Parser();
	public static final ElementParser<LuaFunction> LUA_METHOD_PARSER = new LuaFunction.Parser();

	private final Document document;

	private JavaDocParser(Document doc) {
		document = doc;
	}

	public static JavaDocParser loadURL(String url) throws IOException {
		return new JavaDocParser(Jsoup.connect(url).get());
	}

	public static JavaDocParser loadFile(String path) throws IOException {
		return new JavaDocParser(Jsoup.parse(new File(path), Charset.defaultCharset().name()));
	}

	public List<Method> parseMethods(ElementParser<? extends Method> parser) {

		Elements tables = document.select("table");
		java.util.Set<Element> summaryTables = tables.stream()
				.filter(t -> t.className().equals("memberSummary"))
				.collect(Collectors.toSet());

		Element methodTable = summaryTables.stream()
				.filter(t -> t.attr("summary").startsWith("Method Summary"))
				.collect(Collectors.toSet()).toArray(new Element[]{})[0];

		Elements tableRows = methodTable.getElementsByTag("tr");

		// remove table header
		tableRows.remove(0);

		List<Method> methods = new ArrayList<>();
		for (Element element : tableRows)
		{
			Elements columns = element.getElementsByTag("td");
			String methodText = columns.first().text() + " " + columns.last().text();

			methods.add(parser.parse(methodText));
		}
		return methods;
	}

	public void convertJavaToLuaDoc(Path outputPath) throws IOException {

		List<Method> methods = parseMethods(JavaDocParser.LUA_METHOD_PARSER);
		List<String> lines = new java.util.ArrayList<>();
		for (Method method : methods)
		{
			LuaFunction luaMethod = (LuaFunction) method;
			luaMethod.generateLuaDoc();

			lines.add(luaMethod.toString());
			lines.add("");
		}
		FileUtils.writeLines(outputPath.toFile(), lines, false);
	}

	public static String removeElementQualifier(String element) {
		return element.replaceAll(".\\w+\\.", "");
	}
}
