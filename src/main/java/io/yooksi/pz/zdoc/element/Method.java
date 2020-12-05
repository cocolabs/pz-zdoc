/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.yooksi.pz.zdoc.element;

import io.yooksi.pz.zdoc.doc.JavaDoc;
import io.yooksi.pz.zdoc.lang.ParseResult;

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

		this.modifier = modifier.trim();
		this.returnType = returnType.trim();
		this.name = name.trim();
		this.params = params;
		this.comment = comment.trim();
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
