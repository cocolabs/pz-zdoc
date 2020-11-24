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

import java.io.IOException;

public class JavaDocParser {

	private final Document document;

	private JavaDocParser(Document doc) {
		document = doc;
	}

	public static JavaDocParser create(String url) throws IOException {
		return new JavaDocParser(Jsoup.connect(url).get());
	}

	public static JavaDocParser create(Path path) throws IOException {
		return new JavaDocParser(Jsoup.parse(path.toFile(), Charset.defaultCharset().name()));
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

	public static String removeElementQualifier(String element) {
		return element.replaceAll(".\\w+\\.", "");
	}
}
