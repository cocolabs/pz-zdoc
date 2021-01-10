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

import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.util.ParseUtils;
import io.yooksi.pz.zdoc.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class DetailSignature {

	protected final String signature;

	DetailSignature(String signature) {
		this.signature = normalizeElementText(signature);
	}

	/**
	 * Normalize and trim whitespace of given text. The normalization process also
	 * includes converting non-breaking space ({@code &nbsp}) to whitespace.
	 *
	 * @param text {@code String} to normalize.
	 * @return normalized and trimmed text.
	 */
	private static String normalizeElementText(String text) {

		StringBuilder sb = new StringBuilder();
		char lastChar = 0;
		for (char c : text.toCharArray())
		{
			if (c == 160) {
				c = ' ';
			}
			else if (c == '\r' || c == '\n') {
				continue;
			}
			else if (lastChar == ' ' && c == ' ') {
				continue;
			}
			sb.append(c); lastChar = c;
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
	protected static String normalizeElementText(Element element) {
		return normalizeElementText(element.text());
	}

	public static @Nullable JavaClass parseClassSignature(String signature) {
		return internalParseClassSignature(signature).get(0);
	}

	private static List<JavaClass> internalParseClassSignature(String signature) {

		char[] charArray = signature.toCharArray();
		for (int i1 = 0; i1 < charArray.length; i1++)
		{
			if (charArray[i1] == '<')
			{
				StringBuilder sb = new StringBuilder(charArray.length);
				for (int i2 = i1 + 1; i2 < charArray.length; i2++)
				{
					char c = charArray[i2];
					if (c != '>') sb.append(c);
				}
				String segments = ParseUtils.flushStringBuilder(sb);
				String[] sTypeParameters = new String[2];

				char[] _charArray = segments.toCharArray();
				for (int i3 = 0; i3 < _charArray.length; i3++)
				{
					char c = _charArray[i3];
					if (c == ',')
					{
						sTypeParameters[0] = ParseUtils.flushStringBuilder(sb);
						boolean passedLeadingWhitespace = false;
						for (int i4 = i3 + 1; i4 < _charArray.length; i4++)
						{
							c = _charArray[i4];
							if (passedLeadingWhitespace)
							{
								sb.append(_charArray[i4]);
							}
							else if (c != ' ')
							{
								sb.append(_charArray[i4]);
								passedLeadingWhitespace = true;
							}
						}
						sTypeParameters[1] = sb.toString();
					}
					else sb.append(c);
				}
				Class<?> targetClass = getClassForName(signature.substring(0, i1));
				if (sTypeParameters[0] != null)
				{
					List<JavaClass> typeParameters = new ArrayList<>(2);
					for (String segment : new String[]{ sTypeParameters[0], sTypeParameters[1] })
					{
						typeParameters.addAll(internalParseClassSignature(segment));
					}
					return List.of(new JavaClass(targetClass, typeParameters));
				}
				else return List.of(new JavaClass(targetClass, internalParseClassSignature(segments)));
			}
		}
		List<JavaClass> result = new ArrayList<>();
		Class<?> signatureClass = getClassForName(signature);
		result.add(signature.length() > 1 && signatureClass != null ? new JavaClass(signatureClass) : null);
		return result;
	}

	private static @Nullable Class<?> getClassForName(String name) {
		try {
			return Utils.getClassForName(name);
		}
		catch (ClassNotFoundException e) {
			Logger.error("Unable to get class for name: " + name);
		}
		return null;
	}

	@Override
	public String toString() {
		return signature;
	}
}
