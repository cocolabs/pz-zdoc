package io.yooksi.pz.luadoc.method;

import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.ParseResult;

public abstract class Method implements ParseResult {

	public static final DataParser<JavaMethod, String> JAVA_PARSER = new JavaMethod.Parser();
	public static final DataParser<LuaMethod, Object> LUA_PARSER = new LuaMethod.Parser();

	final String modifier;
	final String returnType;
	final String name;

	final Parameter[] params;

	public Method(String modifier, String returnType, String name, Parameter[] params) {

		this.modifier = modifier;
		this.returnType = returnType;
		this.name = name;
		this.params = params;
	}

	public String getModifier() {
		return modifier;
	}

	public String getReturnType(boolean qualified) {
		return qualified ? returnType : JavaDoc.Parser.removeElementQualifier(returnType);
	}

	public String getName() {
		return name;
	}

	public Parameter[] getParams() {

		Parameter[] result = new Parameter[params.length];
		for (int i = 0; i < params.length; i++) {
			result[i] = params[i].copy();
		}
		return result;
	}

	public abstract String toString();
}
