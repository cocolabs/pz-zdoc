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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.element.JavaClass;
import io.yooksi.pz.zdoc.element.JavaMethod;

public class JavaDocWebParser extends JavaDocParser<URL> {

	private JavaDocWebParser(URL data) throws IOException {
		super(data, Jsoup.connect(data.toString()).get());
	}

	/**
	 * @throws IOException if Jsoup failed connecting to or parsing document.
	 */
	public static JavaDocWebParser create(URL url) throws IOException {
		return new JavaDocWebParser(url);
	}

	/**
	 * @throws IOException if Jsoup failed connecting to or parsing document.
	 * @throws MalformedURLException if url string cannot be converted to URL object.
	 */
	public static JavaDocWebParser create(String url) throws IOException {
		return new JavaDocWebParser(new URL(url));
	}

	@Override
	public JavaDoc<URL> parse() {

		if (data == null) {
			throw new RuntimeException("Tried to parse null data");
		}
		String filename = Paths.get(data.getPath()).getFileName().toString();
		String name = FilenameUtils.getBaseName(filename);

		Set<JavaClass<URL>> members = new java.util.HashSet<>();
		Element summaryTable = getMethodSummary();
		if (summaryTable == null) {
			return new JavaDoc<>(name, members, new java.util.ArrayList<>());
		}
		List<JavaMethod> methods = parseMethods(summaryTable);
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

		return new JavaDoc<>(name, members, methods);
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
