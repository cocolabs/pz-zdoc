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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.yooksi.pz.zdoc.doc.JavaDoc;

/**
 * This class represents an EmmyLua annotation
 *
 * @see <a href="https://emmylua.github.io/">EmmyLua for IntelliJ IDEA</a>
 */
public enum EmmyLua {

	/**
	 * <code>---@class TYPE [:PARENT_TYPE] [@comment]</code>
	 */
	CLASS("class", "---@class [%s] [: %s] [@%s]"),

	/**
	 * <code>---@param param_name TYPE[|other_type] [@comment]</code>
	 */
	PARAM("param", "---@param [%s] [%s][|%s] [@%s]"),

	/**
	 * <code>---@return TYPE[|OTHER_TYPE] [@comment]</code>
	 */
	RETURN("return", "---@return [%s][|%s] [@%s]");

	public static final String[] BUILT_IN_TYPES = {
			"boolean", "string", "number", "userdata",
			"thread", "table", "any", "void", "self"
	};
	public static final String[] RESERVED_KEYWORDS = {
			"and", "break", "do", "else", "elseif", "end",
			"false", "for", "function", "goto", "if", "in",
			"local", "nil", "not", "or", "repeat", "return",
			"then", "true", "until", "while"
	};

	private static final Pattern FORMAT_REGEX = Pattern.compile("\\[.*?%s.*?]");

	private final Pattern keyRegex;
	private final String format;

	EmmyLua(String keyword, String format) {

		this.keyRegex = Pattern.compile("---\\s*@" + keyword);
		this.format = format;
	}

	public static boolean isBuiltInType(String type) {

		String unqualifiedType = JavaDoc.Parser.removeElementQualifier(type);
		type = unqualifiedType.replaceAll("\\[\\s*]", "").toLowerCase();

		for (String builtInType : BUILT_IN_TYPES)
		{
			if (type.equals(builtInType)) {
				return true;
			}
		}
		return false;
	}

	public static String getSafeType(String type) {
		return isBuiltInType(type) ? type.toLowerCase() : type;
	}

	public static boolean isReservedKeyword(String text) {

		text = text.toLowerCase();
		for (String builtInType : RESERVED_KEYWORDS)
		{
			if (text.equals(builtInType)) {
				return true;
			}
		}
		return false;
	}

	public static String getSafeKeyword(String text) {
		return isReservedKeyword(text) ? "var_" + text : text;
	}

	public static String comment(String text) {
		return "--- " + text;
	}

	/**
	 * @return {@code true} if the given text matches this annotation.
	 */
	public boolean isAnnotation(String text) {
		return keyRegex.matcher(text).find();
	}

	/**
	 * Create an EmmyLua annotation with the given parameters.
	 *
	 * @param params array of parameters to use as replacements when formatting.
	 * 		Read the format used by annotations to know how parameters are formatted.
	 * @return a textual representation of this annotation with the given parameters.
	 */
	public String create(String[] params) {

		StringBuffer result = new StringBuffer();
		Matcher match = FORMAT_REGEX.matcher(format);
		for (int i = 0; i < params.length && match.find(); i++)
		{
			String param = match.group().replaceAll("[\\[\\]]", "");
			match.appendReplacement(result, param.replaceFirst("%s", params[i]));
		}
		return result.toString();
	}
}
