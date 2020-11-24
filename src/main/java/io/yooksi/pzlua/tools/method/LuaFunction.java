package io.yooksi.pzlua.tools.method;

import io.yooksi.pzlua.tools.lang.ElementParser;
import io.yooksi.pzlua.tools.parse.JavaDocParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LuaFunction extends Method {

	private final List<String> luaDoc = new ArrayList<>();

	public LuaFunction(String returnType, String name, Parameter[] params) {
		super("", returnType, name, params);
	}

	public static class Parser implements ElementParser<LuaFunction> {

		@Override
		public @Nullable LuaFunction parse(String text) {

			JavaMethod jMethod = JavaDocParser.JAVA_METHOD_PARSER.parse(text);

			Parameter[] lParams = Parameter.getUnqualified(jMethod.params);
			return new LuaFunction(jMethod.returnType, jMethod.name, lParams);
		}
	}

	public List<String> generateLuaDoc() {

		luaDoc.clear();
		for (Parameter param : getParams()) {
			luaDoc.add(String.format("---@param %s %s",
					param.getName(false), param.getType(false)));
		}
		luaDoc.add("---@return " + getReturnType(false));
		return getLuaDoc();
	}

	public List<String> getLuaDoc() {
		return Collections.unmodifiableList(luaDoc);
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		luaDoc.forEach(l -> sb.append(l).append("\n"));
		sb.append("function ").append(name).append('(');

		if (this.params.length > 0)
		{
			Arrays.stream(this.params).forEach(
					p -> sb.append(p.getName(false)).append(", "));
			sb.delete(sb.length() - 2, sb.length());
		}
		return sb.append(')').toString();
	}
}
