/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.element.JavaMethod;
import io.yooksi.pz.zdoc.lang.DataParser;
import io.yooksi.pz.zdoc.lang.ParseRegex;
import io.yooksi.pz.zdoc.logger.Logger;

public abstract class JavaDocParser<T> extends DataParser<JavaDoc<T>, T> {

	protected final Document document;

	protected JavaDocParser(T data, Document document) {
		super(data);
		this.document = document;
	}

	public static String removeElementQualifier(String element) {
		return element.replaceAll("\\w+\\.", "");
	}

	protected Element getMethodSummary() {

		return Objects.requireNonNull(document).select("table").stream()
				.filter(t -> t.className().equals("memberSummary") && t.attr("summary")
						.startsWith("Method Summary")).findFirst().orElse(null);
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
				Logger.warn(String.format("Expected to find method access " +
						"modifier (i=%d) for method %s", i, methodName));
			}
		}
		Logger.error("Unable to parse method access modifier for method " + methodName);
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
			Element[] column = { columns.first(), columns.last() };

			String methodName = column[1].getElementsByClass("memberNameLink").text();
			if (parseMethodAccessModifier(methodName, i).equals("public"))
			{
				String methodText = column[0].text() + " " + column[1].text();

				JavaMethod javaMethod = JavaMethod.Parser.create(methodText).parse();
				/* Do not add methods which were not parsed */
				if (javaMethod != null) {
					methods.add(javaMethod);
				}
			}
			else Logger.warn("Skipping non-public method " + methodName);
		}
		return methods;
	}

	protected List<Element> parseMemberHyperlinks(Element methodSummary) {

		return methodSummary.getElementsByTag("a").stream()
				.filter(e -> e.attr("title").startsWith("class in"))
				.collect(Collectors.toList());
	}
}
