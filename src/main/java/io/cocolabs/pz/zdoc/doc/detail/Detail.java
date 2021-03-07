/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc.doc.detail;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.UnmodifiableView;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.util.ClassUtils;

import io.cocolabs.pz.zdoc.doc.ZomboidAPIDoc;
import io.cocolabs.pz.zdoc.element.IMember;
import io.cocolabs.pz.zdoc.logger.Logger;

public abstract class Detail<T extends IMember> {

	final String name;
	private final ZomboidAPIDoc document;
	private final List<T> entries;

	Detail(String name, ZomboidAPIDoc document) throws DetailParsingException {
		this.name = name;
		this.document = document;
		this.entries = Collections.unmodifiableList(parse());
	}

	Elements getDetail() {

		Optional<Element> detailElement = document.getDocument().getElementsByTag("a")
				.stream().filter(e -> e.hasAttr("name") &&
						e.attr("name").equals(name)).findFirst();

		if (detailElement.isPresent()) {
			return detailElement.get().parent().getElementsByTag("ul");
		}
		else return new Elements();
	}

	Element qualifyZomboidClassElements(Element element) throws DetailParsingException {

		for (Element e : element.getElementsByTag("a"))
		{
			List<TextNode> textNodes = e.textNodes();
			if (textNodes.size() != 1)
			{
				String format = "Unexpected number of hyperlink text nodes (%s)";
				throw new DetailParsingException(this, String.format(format, textNodes.size()));
			}
			TextNode textNode = textNodes.get(0);
			String absUrl = e.absUrl("href");
			if (StringUtils.isBlank(absUrl))
			{
				String format = "Missing href for node \"%s\"";
				throw new DetailParsingException(this, String.format(format, textNode.text()));
			}
			String urlPath = ZomboidAPIDoc.resolveURLPath(absUrl).toString();
			/*
			 * remove file extension from API URL
			 * "/zombie/Zombie.html" -> "/zombie/Zombie"
			 */
			urlPath = urlPath.substring(0, FilenameUtils.indexOfExtension(urlPath));
			/*
			 * ClassUtils#convertResourcePathToClassName path parameter
			 * needs to be in Unix format, otherwise we get a malformed return value
			 */
			String packagePath = urlPath.replace('\\', '/');
			textNode.text(ClassUtils.convertResourcePathToClassName(packagePath));
		}
		return element;
	}

	/**
	 * Parse comment text from given element.
	 */
	static String parseDetailComments(Element element) {

		StringBuilder commentBuilder = new StringBuilder();
		Elements commentBlocks = element.getElementsByClass("block");
		if (!commentBlocks.isEmpty())
		{
			commentBuilder.append(commentBlocks.get(0).text());
			/*
			 * normally there should only be one comment block per element
			 * but check for additional blocks just to be on the safe side
			 */
			for (int i = 1; i < commentBlocks.size(); i++) {
				commentBuilder.append('\n').append(commentBlocks.get(i).text());
			}
		}
		String result = commentBuilder.toString();
		if (!result.isEmpty()) {
			Logger.debug("Parsed detail comment: \"" + result + "\"");
		}
		return result;
	}

	public @UnmodifiableView List<T> getEntries() {
		return entries;
	}

	public abstract Set<T> getEntries(String name);

	protected abstract List<T> parse() throws DetailParsingException;
}
