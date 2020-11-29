package io.yooksi.pz.luadoc.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.EmmyLua;

/**
 * This class represents a parsed Lua method.
 */
public class LuaMethod extends Method {

	private final List<String> luaDoc = new ArrayList<>();

	public LuaMethod(String returnType, String name, Parameter[] params, String comment) {
		super("", returnType, name, params, comment);
	}

	public LuaMethod(String returnType, String name, Parameter[] params) {
		super(returnType, name, params);
	}

	public List<String> annotate() {

		luaDoc.clear();
		if (hasComment()) {
			luaDoc.add(EmmyLua.comment(getComment()));
		}
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

	public static class Parser extends DataParser<LuaMethod, JavaMethod> {

		private Parser(JavaMethod data) {
			super(data);
		}

		public static Parser create(JavaMethod javaMethod) {
			return new Parser(javaMethod);
		}

		@Override
		public LuaMethod parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			return new LuaMethod(data.getReturnType(false),
					data.getName(), data.getParams(), data.getComment());
		}
	}
}
