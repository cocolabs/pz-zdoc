package io.yooksi.pz.luadoc.lang;

import java.util.regex.Pattern;

public class ParseRegex {

	/** Regex pattern used to parse java method from text */
	public static final Pattern JAVA_METHOD_REGEX = Pattern.compile("^((?:final|static|abstract|" +
			"transient|synchronized|volatile)\\s+)?([^\\s]+)\\s+([^\\s]+)\\((.+)?\\)$");

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
