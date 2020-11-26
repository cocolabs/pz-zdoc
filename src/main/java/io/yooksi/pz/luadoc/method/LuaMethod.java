package io.yooksi.pz.luadoc.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.EmmyLua;

public class LuaMethod extends Method {

	private final List<String> luaDoc = new ArrayList<>();

	public LuaMethod(String returnType, String name, Parameter[] params) {
		super("", returnType, name, params);
	}

	public List<String> annotate() {

		luaDoc.clear();
		for (Parameter param : getParams())
		{
			luaDoc.add(EmmyLua.PARAM.create(new String[]{
					param.getName(false),
					param.getType(false)
			}));
		}
		luaDoc.add(EmmyLua.RETURN.create(new String[]{ getReturnType(false) }));
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

	public static class Parser extends DataParser<LuaMethod, Object> {

		@Override
		public LuaMethod parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			if (data instanceof String)
			{
				JavaMethod jMethod = Method.JAVA_PARSER.input((String) data).parse();

				Parameter[] lParams = Parameter.getUnqualified(jMethod.params);
				return new LuaMethod(jMethod.returnType, jMethod.name, lParams);
			}
			else if (data instanceof JavaMethod)
			{
				JavaMethod javaMethod = (JavaMethod) data;
				return new LuaMethod(javaMethod.getReturnType(false),
						javaMethod.getName(), javaMethod.getParams());
			}
			else throw new UnsupportedOperationException("Cannot parse unknown " +
						"data type: " + data.getClass().getName());
		}
	}
}
