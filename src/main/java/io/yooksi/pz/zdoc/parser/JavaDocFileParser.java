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
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.element.JavaClass;
import io.yooksi.pz.zdoc.element.JavaMethod;

public class JavaDocFileParser extends JavaDocParser<Path> {

	private JavaDocFileParser(Path data) throws IOException {
		super(data, Jsoup.parse(data.toFile(), Charset.defaultCharset().name()));
	}

	/**
	 * @throws IOException if the file could not be found or read.
	 * @throws InvalidPathException if the path string cannot be converted to a {@code Path}.
	 */
	public static JavaDocFileParser create(String path) throws IOException {
		return new JavaDocFileParser(Paths.get(path));
	}

	/**
	 * @throws IOException if the file could not be found or read.
	 */
	public static JavaDocFileParser create(Path path) throws IOException {
		return new JavaDocFileParser(path);
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
