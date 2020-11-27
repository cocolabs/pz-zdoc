package io.yooksi.pz.luadoc.element;

import io.yooksi.pz.luadoc.doc.JavaDoc;

/**
 * This class represents parsed method parameter.
 */
public class Parameter {

	private final String type;
	private final String name;

	public Parameter(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public static Parameter[] getUnqualified(Parameter[] params) {

		Parameter[] result = new Parameter[params.length];
		for (int i = 0; i < params.length; i++) {
			result[i] = new Parameter(params[i].getType(false),
					params[i].getName(false));
		}
		return result;
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
