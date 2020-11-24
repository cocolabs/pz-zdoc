package io.yooksi.pzlua.tools.method;

import io.yooksi.pzlua.tools.lang.ParseResult;
import io.yooksi.pzlua.tools.parse.JavaDocParser;

public abstract class Method implements ParseResult {

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
		return qualified ? returnType : JavaDocParser.removeElementQualifier(returnType);
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
