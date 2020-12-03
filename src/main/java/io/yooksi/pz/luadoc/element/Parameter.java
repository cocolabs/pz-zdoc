package io.yooksi.pz.luadoc.element;

import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.lang.EmmyLua;

/**
 * This class represents parsed method parameter.
 */
public class Parameter {

	private final String type;
	private final String name;

	public Parameter(String type, String name) {

		type = type.trim();
		// ensure built-in types are lower-cased
		this.type = EmmyLua.getSafeType(type);

		name = name.trim();
		// ensure parameter name is not a reserved lua keyword
		this.name = EmmyLua.getSafeKeyword(name);
	}

	@Override
	public String toString() {
		return (type + ' ' + name).trim();
	}

	public String getType(boolean qualified) {
		return qualified ? type : JavaDoc.Parser.removeElementQualifier(type);
	}

	public String getName(boolean qualified) {
		return qualified ? name : JavaDoc.Parser.removeElementQualifier(name);
	}

	public Parameter getUnqualified() {
		return new Parameter(getType(false), getName(false));
	}

	public Parameter copy() {
		return new Parameter(type, name);
	}
}
