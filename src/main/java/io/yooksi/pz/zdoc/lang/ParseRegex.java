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
package io.yooksi.pz.zdoc.lang;

import java.util.regex.Pattern;

public class ParseRegex {

	/**
	 * Regex pattern used to parse java method from text
	 */
	public static final Pattern JAVA_METHOD_REGEX = Pattern.compile(
			"^((?:final|static|abstract|transient|synchronized|volatile)" +
					"\\s+)?([^\\s]+)\\s+([^\\s]+)\\(([^)]+)?\\)(.*)?"
	);
	public static final Pattern LUA_TABLE_DECLARATION_REGEX = Pattern.compile(
			"^(\\s*)(\\w+)\\s+=(?:\\s+\\{\\s*}|.*:new\\(|.*:derive\\().*$"
	);
	public static final Pattern FIRST_WORD_REGEX = Pattern.compile("^(\\w+).*");

	public static final Pattern OBJECT_TYPE_REGEX = Pattern.compile("([^><]+)[><]");

	/**
	 * @param matcher process that contains regex groups
	 * @param i index of group to retrieve
	 * @return the matched group <i>or</i> an <b>empty</b> string
	 */
	public static String getMatchedGroup(java.util.regex.Matcher matcher, int i) {

		String result = matcher.group(i);
		return result != null ? result : "";
	}
}
