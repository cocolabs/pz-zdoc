/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc.element.java;

import java.lang.reflect.Parameter;

import io.cocolabs.pz.zdoc.element.IParameter;
import io.cocolabs.pz.zdoc.element.SignatureToken;

public class JavaParameter implements IParameter, SignatureToken {

	private final JavaClass type;
	private final String name;

	public JavaParameter(JavaClass type, String name) {
		this.type = type;
		this.name = name;
	}

	public JavaParameter(Class<?> type, String name) {
		this(new JavaClass(type), name);
	}

	public JavaParameter(Parameter parameter) {
		this(new JavaClass(parameter.getType()), parameter.getName());
	}

	@Override
	public String toString() {
		return (type.toString() + ' ' + getName()).trim();
	}

	@Override
	public String getAsVarArg() {

		String sType = type.toString();
		if (sType.endsWith("[]")) {
			sType = sType.substring(0, sType.length() - 2);
		}
		return (sType + "... " + getName()).trim();
	}

	@Override
	public JavaClass getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings("ReferenceEquality")
	public boolean equals(JavaParameter param, boolean shallow) {

		if (shallow)
		{
			if (this == param) {
				return true;
			}
			if (param == null) {
				return false;
			}
			return type.equals(param.type, true);
		}
		else return equals(param);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (!(obj instanceof JavaParameter)) {
			return false;
		}
		JavaParameter param = (JavaParameter) obj;
		return name.equals(param.name) && type.equals(param.type);
	}

	@Override
	public int hashCode() {
		return 31 * type.hashCode() + name.hashCode();
	}
}
