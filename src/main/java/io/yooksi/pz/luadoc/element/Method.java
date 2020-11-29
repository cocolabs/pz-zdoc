package io.yooksi.pz.luadoc.element;

import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.lang.ParseResult;

/**
 * This class represents a parsed code method.
 */
@SuppressWarnings("unused")
public abstract class Method implements ParseResult {

	final String modifier;
	final String returnType;
	final String name;

	final Parameter[] params;
	final String comment;

	public Method(String modifier, String returnType, String name, Parameter[] params, String comment) {

		this.modifier = modifier;
		this.returnType = returnType;
		this.name = name;
		this.params = params;
		this.comment = comment;
	}

	public Method(String modifier, String returnType, String name, Parameter[] params) {
		this(modifier, returnType, name, params, "");
	}

	public Method(String returnType, String name, Parameter[] params) {
		this("", returnType, name, params, "");
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

	public boolean hasComment() {
		return !comment.isEmpty();
	}

	public String getComment() {
		return comment;
	}

	public abstract String toString();
}
