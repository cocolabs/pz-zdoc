package io.yooksi.pz.luadoc.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.lang.ElementParser;
import io.yooksi.pz.luadoc.lang.EmmyLua;
import io.yooksi.pz.luadoc.parse.JavaDocParser;

public class LuaMethod extends Method {

	private final List<String> luaDoc = new ArrayList<>();

	public LuaMethod(String returnType, String name, Parameter[] params) {
		super("", returnType, name, params);
	}

	public List<String> generateLuaDoc() {

		luaDoc.clear();
		for (Parameter param : getParams()) {
			luaDoc.add(EmmyLua.PARAM.create(param.getName(false), param.getType(false)));
		}
		luaDoc.add(EmmyLua.RETURN.create(getReturnType(false)));
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

	public static class Parser implements ElementParser<LuaMethod> {

		@Override
		public @Nullable LuaMethod parse(String text) {

			JavaMethod jMethod = JavaDocParser.JAVA_METHOD_PARSER.parse(text);

			Parameter[] lParams = Parameter.getUnqualified(jMethod.params);
			return new LuaMethod(jMethod.returnType, jMethod.name, lParams);
		}
	}
}
