package io.yooksi.pzlua.tools.lang;

import java.util.regex.Pattern;

public enum EmmyLuaAnnotation {

	CLASS("class") {
		@Override
		public String create(String name, String...params) {
			return getBase() + ' ' + name;
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

		Pattern regex = Pattern.compile(getBase());
		return regex.matcher(text).find();
	}

	public abstract String create(String name, String...params);
}
