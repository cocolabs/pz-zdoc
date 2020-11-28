package io.yooksi.pz.luadoc.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern FORMAT_REGEX = Pattern.compile("\\[.*?%s.*?]");

	private final Pattern keyRegex;
	private final String format;

	EmmyLua(String keyword, String format) {

		this.keyRegex = Pattern.compile("---\\s*@" + keyword);
		this.format = format;
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
