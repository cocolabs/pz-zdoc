package io.yooksi.pzlua.tools.lang;

import java.util.regex.Pattern;

public enum EmmyLuaAnnotation {

	CLASS("class") {
		@Override
		public String create(String name, String...params) {

			String annotation = getBase() + ' ' + name;
			if (params.length > 0 && !params[0].isEmpty()) {
				annotation += " : " + params[0];
			}
			return annotation;
		}
	};

	private final String keyword;
	EmmyLuaAnnotation(String keyword) {
		this.keyword = keyword;
	}

	public String getBase() {
		return "---@" + keyword;
	}

	public boolean isAnnotation(String text) {

		Pattern regex = Pattern.compile("---\\s*@");
		return regex.matcher(text).find();
	}

	public abstract String create(String name, String...params);
}
