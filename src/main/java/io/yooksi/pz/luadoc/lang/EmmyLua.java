package io.yooksi.pz.luadoc.lang;

import java.util.regex.Pattern;

public enum EmmyLua {

	CLASS("class")
			{
				@Override
				public String create(String name, String... params) {

					String annotation = getBase() + ' ' + name;
					String parentType = params.length > 0 ? params[0] : "";

					if (!parentType.isEmpty()) {
						annotation += " : " + parentType.trim();
					}
					return annotation;
				}
			},
	PARAM("param")
			{
				@Override
				public String create(String name, String... params) {

					String annotation = getBase() + ' ' + name;
					String otherType = params.length > 0 ? params[0] : "";

					if (!otherType.isEmpty()) {
						annotation += ' ' + otherType.trim();
					}
					return annotation;
				}
			},
	RETURN("return")
			{
				@Override
				public String create(String name, String... params) {

					String annotation = getBase() + ' ' + name;
					String type = params.length > 0 ? params[0] : "";

					if (!type.isEmpty()) {
						annotation += ' ' + type.trim();
					}
					return annotation;
				}
			};

	private final String keyword;

	EmmyLua(String keyword) {
		this.keyword = keyword;
	}

	public String getBase() {
		return "---@" + keyword;
	}

	public boolean isAnnotation(String text) {

		Pattern regex = Pattern.compile("---\\s*@");
		return regex.matcher(text).find();
	}

	public abstract String create(String name, String... params);
}
