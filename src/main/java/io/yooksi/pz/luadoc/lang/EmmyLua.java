package io.yooksi.pz.luadoc.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EmmyLua {

	/**
	 * <code>---@class TYPE [:PARENT_TYPE] [@comment]</code>
	 */
	CLASS("class", "---@class [%s] [: %s] [@%s]"),

	/**
	 * <code>---@param param_name TYPE[|other_type] [@comment]</code>
	 */
	PARAM("class", "---@param [%s] [%s][|%s] [@%s]"),

	/**
	 * <code>---@return TYPE[|OTHER_TYPE] [@comment]</code>
	 */
	RETURN("class", "---@return [%s][|%s] [@%s]");

	private static final Pattern FORMAT_REGEX = Pattern.compile("\\[.*?%s.*?]");

	private final Pattern keyRegex;
	private final String format;

	EmmyLua(String keyword, String format) {

		this.keyRegex = Pattern.compile("---\\s*@" + keyword);
		this.format = format;
	}

	public boolean isAnnotation(String text) {
		return keyRegex.matcher(text).find();
	}

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
