package io.yooksi.pzlua.tools.parse;

import org.jsoup.Jsoup;

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


	public static String removeElementQualifier(String element) {
		return element.replaceAll(".\\w+\\.", "");
	}
}
