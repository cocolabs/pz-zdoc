package io.yooksi.pz.luadoc.parse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.luadoc.lang.ElementParser;
import io.yooksi.pz.luadoc.method.JavaMethod;
import io.yooksi.pz.luadoc.method.LuaMethod;
import io.yooksi.pz.luadoc.method.Method;

public class JavaDocParser {

	public static final String PZ_API_URL = "https://projectzomboid.com/modding/";
	public static final String PZ_API_GLOBAL_URL = PZ_API_URL + "Lua/LuaManager.GlobalObject.html";

	public static final ElementParser<JavaMethod> JAVA_METHOD_PARSER = new JavaMethod.Parser();
	public static final ElementParser<LuaMethod> LUA_METHOD_PARSER = new LuaMethod.Parser();

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

	public static String removeElementQualifier(String element) {
		return element.replaceAll(".\\w+\\.", "");
	}

	public <T extends Method> List<T> parseMethods(ElementParser<T> parser) {

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

		List<T> methods = new ArrayList<>();
		for (Element element : tableRows)
		{
			Elements columns = element.getElementsByTag("td");
			String methodText = columns.first().text() + " " + columns.last().text();

			methods.add(parser.parse(methodText));
		}
		return methods;
	}

	public void convertJavaToLuaDoc(Path outputPath) throws IOException {

		List<String> lines = new java.util.ArrayList<>();
		for (Method method : parseMethods(JavaDocParser.LUA_METHOD_PARSER))
		{
			LuaMethod luaMethod = (LuaMethod) method;
			luaMethod.generateLuaDoc();

			lines.add(luaMethod.toString());
			lines.add("");
		}
		lines.remove(lines.size() - 1);
		FileUtils.writeLines(outputPath.toFile(), lines, false);
	}
}
