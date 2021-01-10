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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.element.IClass;

/**
 * This class represents a parsed Java class.
 */
public class JavaClass implements IClass {

	private final Class<?> clazz;
	private final List<JavaClass> typeParameters;

	public JavaClass(Class<?> clazz, @Nullable List<JavaClass> typeParameters) {
		this.clazz = clazz;
		this.typeParameters = Collections.unmodifiableList(
				Optional.ofNullable(typeParameters).orElse(new ArrayList<>())
		);
	}

	public JavaClass(Class<?> clazz, int typeParameterCount) {
		this(clazz, getUnknownTypeParameterList(typeParameterCount));
	}

	public JavaClass(Class<?> clazz) {
		this.clazz = clazz;
		int length = clazz.getTypeParameters().length;
		this.typeParameters = Collections.unmodifiableList(
				length != 0 ? getUnknownTypeParameterList(length) : new ArrayList<>()
		);
	}

	static @UnmodifiableView List<JavaClass> getUnknownTypeParameterList(int size) {

		Validate.inclusiveBetween(1, 2, size);
		List<JavaClass> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(null);
		}
		return Collections.unmodifiableList(result);
	}

	public static String getPathForClass(Class<?> clazz) {

		if (clazz.getPackage() != null)
		{
			char[] className = clazz.getName().toCharArray();
			char[] cClassPath = new char[className.length];
			for (int i = 0; i < className.length; i++)
			{
				char c = className[i];
				if (c == '.') {
					cClassPath[i] = '/';
				}
				else if (c == '$') {
					cClassPath[i] = '.';
				}
				else cClassPath[i] = c;
			}
			return new String(cClassPath);
		}
		else return "";
	}

	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return clazz.getTypeName();
	}

	@Override
	public @UnmodifiableView List<JavaClass> getTypeParameters() {
		return typeParameters;
	}

	private String readTypeParameter(int index) {

		JavaClass parameter = typeParameters.get(index);
		return parameter != null ? parameter.toString() : "?";
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if (!typeParameters.isEmpty())
		{
			sb.append('<');
			sb.append(readTypeParameter(0));
			for (int i = 1; i < typeParameters.size(); i++) {
				sb.append(", ").append(readTypeParameter(i));
			}
			sb.append('>');
		}
		return sb.toString();
	}

	public boolean equals(JavaClass jClass, boolean shallow) {

		if (shallow)
		{
			if (this == jClass) {
				return true;
			}
			if (jClass == null) {
				return false;
			}
			if (!clazz.equals(jClass.clazz)) {
				return false;
			}
			return typeParameters.size() == jClass.typeParameters.size();
		}
		else return equals(jClass);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JavaClass jClass = (JavaClass) obj;
		return clazz.equals(jClass.clazz) && jClass.typeParameters.equals(typeParameters);
	}

	@Override
	public int hashCode() {
		return 31 * clazz.hashCode() + typeParameters.hashCode();
	}
}
