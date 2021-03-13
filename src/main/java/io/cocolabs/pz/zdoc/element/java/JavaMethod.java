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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import io.cocolabs.pz.zdoc.element.IMethod;
import io.cocolabs.pz.zdoc.element.mod.MemberModifier;
import io.cocolabs.pz.zdoc.logger.Logger;

/**
 * This class represents a wrapped {@link Method} object.
 */
public class JavaMethod implements IMethod {

	private final String name;
	private final ReturnType returnType;
	private final List<JavaParameter> params;
	private final MemberModifier modifier;
	private final boolean hasVarArg;
	private final String comment;

	public JavaMethod(Builder builder) {

		this.name = builder.name;
		this.returnType = builder.returnType != null ? builder.returnType : new ReturnType(void.class);
		this.modifier = builder.modifier != null ? builder.modifier : MemberModifier.UNDECLARED;
		List<JavaParameter> jParams = builder.params != null ? builder.params : new ArrayList<>();
		if (builder.hasVarArg)
		{
			if (!jParams.isEmpty())
			{
				List<JavaParameter> tParams = new ArrayList<>();
				/*
				 * copy every element in params list except last one,
				 * which has to be copied separately below
				 */
				int lastIndex = jParams.size() - 1;
				for (int i = 0; i < lastIndex; i++) {
					tParams.add(jParams.get(i));
				}
				JavaParameter lastParam = jParams.get(lastIndex);
				JavaClass paramType = lastParam.getType();
				/*
				 * copy last parameter with an array of the original
				 * class to match variadic argument in bytecode
				 */
				Class<?> arrayClass = Array.newInstance(paramType.getClazz(), 0).getClass();
				JavaClass newJClass = new JavaClass(arrayClass, paramType.getTypeParameters());

				tParams.add(new JavaParameter(newJClass, lastParam.getName()));
				this.params = Collections.unmodifiableList(tParams);
			}
			else {
				builder.hasVarArg = false;
				// initialize params before logging error with toString() to avoid NPE
				this.params = Collections.unmodifiableList(jParams);
				Logger.error("Method %s marked with hasVarArg with no parameters", toString());
			}
		}
		else this.params = Collections.unmodifiableList(jParams);
		this.hasVarArg = builder.hasVarArg;
		this.comment = builder.comment;
	}

	public JavaMethod(Method method) {

		this.name = method.getName();
		this.returnType = new ReturnType(method.getReturnType());

		List<JavaParameter> params = new ArrayList<>();
		for (Parameter methodParam : method.getParameters()) {
			params.add(new JavaParameter(methodParam));
		}
		this.params = Collections.unmodifiableList(params);
		this.modifier = new MemberModifier(method.getModifiers());
		this.hasVarArg = false;
		this.comment = "";
	}

	@Override
	public String toString() {

		String sParams = "";
		if (this.params.size() > 0)
		{
			final StringBuilder sb = new StringBuilder();
			int lastElementIndex = params.size() - 1;
			// method has 2 or more parameters
			if (lastElementIndex > 0)
			{
				sb.append(params.get(0).toString());
				for (int i = 1; i < lastElementIndex; i++) {
					sb.append(", ").append(params.get(i).toString());
				}
				sb.append(", ");
			}
			JavaParameter lastParameter = params.get(lastElementIndex);
			sb.append(!hasVarArg ? lastParameter.toString() : lastParameter.getAsVarArg());
			sParams = sb.toString();
		}
		String modifier = this.modifier.toString();
		modifier = modifier.isEmpty() ? "" : modifier + " ";
		return String.format("%s%s %s(%s)", modifier, returnType, getName(), sParams);
	}

	public static class Builder {

		private final String name;
		private @Nullable ReturnType returnType;
		private @Nullable MemberModifier modifier;
		private @Nullable List<JavaParameter> params;

		private boolean hasVarArg = false;
		private String comment = "";

		public Builder(String name) {
			this.name = name;
		}

		public static Builder create(String name) {
			return new Builder(name);
		}

		public Builder withReturnType(JavaClass type, String comment) {
			returnType = new ReturnType(type, comment);
			return this;
		}

		public Builder withReturnType(JavaClass type) {
			returnType = new ReturnType(type, "");
			return this;
		}

		public Builder withReturnType(Class<?> type) {
			returnType = new ReturnType(type);
			return this;
		}

		public Builder withParams(List<JavaParameter> params) {
			this.params = params;
			return this;
		}

		public Builder withModifier(MemberModifier modifier) {
			this.modifier = modifier;
			return this;
		}

		public Builder withParams(JavaParameter...params) {
			this.params = new ArrayList<>(Arrays.asList(params));
			return this;
		}

		public Builder withVarArgs(boolean hasVarArg) {
			this.hasVarArg = hasVarArg;
			return this;
		}

		public Builder withComment(String comment) {
			this.comment = comment;
			return this;
		}

		public JavaMethod build() {
			return new JavaMethod(this);
		}
	}

	public static class ReturnType extends JavaClass {

		private final String comment;

		public ReturnType(Class<?> clazz, @Nullable List<JavaClass> typeParameters, String comment) {
			super(clazz, typeParameters);
			this.comment = comment;
		}

		public ReturnType(Class<?> clazz, @Nullable List<JavaClass> typeParameters) {
			this(clazz, typeParameters, "");
		}

		public ReturnType(JavaClass clazz, String comment) {
			this(clazz.getClazz(), clazz.getTypeParameters(), comment);
		}

		public ReturnType(Class<?> clazz) {
			super(clazz);
			this.comment = "";
		}

		public String getComment() {
			return comment;
		}
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
	public ReturnType getReturnType() {
		return returnType;
	}

	@Override
	public @UnmodifiableView List<JavaParameter> getParams() {
		return params;
	}

	@Override
	public boolean hasVarArg() {
		return hasVarArg;
	}

	@SuppressWarnings("ReferenceEquality")
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
		if (!(obj instanceof JavaMethod)) {
			return false;
		}
		JavaMethod jMethod = (JavaMethod) obj;
		if (name.equals(jMethod.name) && returnType.equals(jMethod.returnType)) {
			return true;
		}
		if (hasVarArg != jMethod.hasVarArg) {
			return false;
		}
		return params.equals(jMethod.params) && modifier.equals(jMethod.modifier);
	}

	@Override
	public int hashCode() {

		int result = 31 * name.hashCode() + returnType.hashCode();
		result = 31 * result + params.hashCode();
		result = 31 * result + modifier.hashCode();
		return 31 * result + (hasVarArg ? 1 : 0);
	}
}
