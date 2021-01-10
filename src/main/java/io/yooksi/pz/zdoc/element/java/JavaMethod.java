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
package io.yooksi.pz.zdoc.element.java;

import io.yooksi.pz.zdoc.element.IMethod;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a wrapped {@link Method} object.
 */
public class JavaMethod implements IMethod {

	private final String name;
	private final JavaClass returnType;
	private final List<JavaParameter> params;
	private final MemberModifier modifier;
	private final String comment;

	public JavaMethod(String name, JavaClass returnType,
					  List<JavaParameter> params, MemberModifier modifier, String comment) {
		this.name = name;
		this.returnType = Validate.notNull(returnType);
		this.params = Collections.unmodifiableList(params);
		this.modifier = modifier;
		this.comment = comment;
	}

	public JavaMethod(String name, Class<?> returnType,
					  List<JavaParameter> params, MemberModifier modifier, String comment) {
		this(name, new JavaClass(returnType), params, modifier, comment);
	}

	public JavaMethod(String name, JavaClass returnType,
					  List<JavaParameter> params, MemberModifier modifier) {
		this(name, returnType, params, modifier, "");
	}

	public JavaMethod(String name, JavaClass returnType, MemberModifier modifier) {
		this(name, returnType, List.of(), modifier, "");
	}

	public JavaMethod(String name, Class<?> returnType,
					  List<JavaParameter> params, MemberModifier modifier) {
		this(name, new JavaClass(returnType), params, modifier, "");
	}

	public JavaMethod(String name, Class<?> returnType, MemberModifier modifier) {
		this(name, new JavaClass(returnType), List.of(), modifier, "");
	}

	public JavaMethod(Method method) {

		this.name = method.getName();
		this.returnType = new JavaClass(method.getReturnType());

		List<JavaParameter> params = new ArrayList<>();
		for (Parameter methodParam : method.getParameters()) {
			params.add(new JavaParameter(methodParam));
		}
		this.params = Collections.unmodifiableList(params);
		this.modifier = new MemberModifier(method.getModifiers());
		this.comment = "";
	}

	@Override
	public String toString() {

		String sParams = "";
		if (this.params.size() > 0)
		{
			final StringBuilder sb = new StringBuilder();
			params.forEach(p -> sb.append(p.toString()).append(", "));
			sParams = sb.substring(0, sb.length() - 2).trim();
		}
		return String.format("%s %s %s(%s)", getModifier(), returnType, getName(), sParams);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MemberModifier getModifier() {
		return modifier;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public JavaClass getReturnType() {
		return returnType;
	}

	@Override
	public @UnmodifiableView List<JavaParameter> getParams() {
		return params;
	}

	public boolean equals(JavaMethod method, boolean shallow) {

		if (shallow)
		{
			if (this == method) {
				return true;
			}
			if (method == null) {
				return false;
			}
			if (!name.equals(method.name)) {
				return false;
			}
			if (!returnType.equals(method.returnType, true)) {
				return false;
			}
			else if (!modifier.equals(method.modifier)) {
				return false;
			}
			if (params.size() != method.params.size()) {
				return false;
			}
			for (int i = 0; i < params.size(); i++)
			{
				if (!params.get(i).equals(method.params.get(i), true)) {
					return false;
				}
			}
			return true;
		}
		else return equals(method);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JavaMethod jMethod = (JavaMethod) obj;
		if (name.equals(jMethod.name) && returnType.equals(jMethod.returnType)) {
			return true;
		}
		return params.equals(jMethod.params) && modifier.equals(jMethod.modifier);
	}

	@Override
	public int hashCode() {

		int result = 31 * name.hashCode() + returnType.hashCode();
		result = 31 * result + params.hashCode();
		return 31 * result + modifier.hashCode();
	}
}
