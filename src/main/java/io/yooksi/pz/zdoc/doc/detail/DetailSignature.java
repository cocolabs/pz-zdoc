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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;

import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.util.ParseUtils;
import io.yooksi.pz.zdoc.util.Utils;

abstract class DetailSignature {

	final String signature;

	DetailSignature(String signature) {
		this.signature = normalizeSignature(signature);
	}

	/**
	 * Normalize given signature text:
	 * <ul>
	 * <li>Convert non-breaking space ({@code &nbsp}) to whitespace.</li>
	 * <li>Remove newlines.</li>
	 * <li>Remove consecutive whitespaces.</li>
	 * </ul>
	 *
	 * @param text {@code String} to normalize.
	 * @return normalized text.
	 */
	private static String normalizeSignature(String text) {

		StringBuilder sb = new StringBuilder();
		char lastChar = 0;
		for (char c : text.toCharArray())
		{
			// convert &nbsp to whitespace
			if (c == 160) {
				c = ' ';
			}
			// remove newlines
			else if (c == '\r' || c == '\n') {
				continue;
			}
			// remove consecutive whitespaces
			else if (lastChar == ' ' && c == ' ') {
				continue;
			}
			sb.append(c);
			lastChar = c;
		}
		return sb.toString();
	}

	/**
	 * Gets the combined text of the given element and all its children.
	 * Whitespace is normalized and trimmed. The normalization process also
	 * includes converting non-breaking space ({@code &nbsp}) to whitespace.
	 *
	 * @param element {@code Element} text to normalize.
	 * @return normalized and trimmed text or empty text if none.
	 *
	 * @see Element#text()
	 */
	static String normalizeElement(Element element) {
		return normalizeSignature(element.text());
	}

	public static @Nullable JavaClass parseClassSignature(String signature) {

		List<JavaClass> result = new JavaClassBuilder(signature).build();
		return !result.isEmpty() ? result.get(0) : null;
	}

	private static @Nullable JavaClass getClassForName(String name) {
		try {
			return new JavaClass(Utils.getClassForName(name));
		}
		catch (ClassNotFoundException e) {
			Logger.debug("Failed to get class for name: " + name);
		}
		return null;
	}

	@Override
	public String toString() {
		return signature;
	}

	private static class JavaClassBuilder {

		private final String signature;
		private final StringBuilder sb;
		private final AtomicInteger index;
		private final List<JavaClass> result;

		private JavaClassBuilder(String signature, AtomicInteger index) {
			this.signature = signature;
			this.sb = new StringBuilder();
			this.result = new ArrayList<>();
			this.index = index;
		}

		private JavaClassBuilder(String signature) {
			this(signature, new AtomicInteger());
		}

		private List<JavaClass> build() {

			char[] charArray = signature.toCharArray();
			for (; index.get() < charArray.length; index.getAndIncrement())
			{
				char c = charArray[index.get()];
				if (c == '<')
				{
					JavaClass type = getClassForName(flush());
					if (type == null) {
						return result;
					}
					index.incrementAndGet();
					List<JavaClass> params = new JavaClassBuilder(signature, index).build();
					result.add(new JavaClass(type.getClazz(), params));
				}
				else if (c == ',') {
					flushToResult();
				}
				else if (c == '>') {
					return flushToResult();
				}
				else if (c != ' ') {
					sb.append(c);
				}
			}
			if (result.isEmpty() && sb.length() > 0) {
				result.add(getClassForName(sb.toString()));
			}
			return result;
		}

		private String flush() {
			return ParseUtils.flushStringBuilder(sb);
		}

		private List<JavaClass> flushToResult() {

			String name = flush();
			if (!name.isEmpty()) {
				result.add(getClassForName(name));
			}
			return result;
		}
	}
}
