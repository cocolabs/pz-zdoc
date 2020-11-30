package io.yooksi.pz.luadoc.lang;

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
