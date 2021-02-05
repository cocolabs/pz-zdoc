/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2021 Matthew Cain
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
package io.yooksi.pz.zdoc.element.lua;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.element.IMethod;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaAccess;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaReturn;
import io.yooksi.pz.zdoc.lang.lua.EmmyLuaVarArg;
import io.yooksi.pz.zdoc.logger.Logger;

/**
 * This class represents a parsed Lua method.
 */
public class LuaMethod implements IMethod, Annotated {

	private final @Nullable LuaClass owner;

	private final String name;
	private final LuaType returnType;
	private final List<LuaParameter> params;
	private final MemberModifier modifier;
	private final boolean hasVarArg;
	private final List<EmmyLua> annotations;
	private final String comment;

	private LuaMethod(Builder builder) {

		this.name = EmmyLua.getSafeLuaName(builder.name);
		this.owner = builder.owner;
		this.returnType = builder.returnType != null ? builder.returnType : new LuaType("void");
		this.modifier = builder.modifier != null ? builder.modifier : MemberModifier.UNDECLARED;
		this.params = Collections.unmodifiableList(builder.params);

		List<EmmyLua> annotations = new ArrayList<>();
		if (!modifier.hasAccess(AccessModifierKey.DEFAULT)) {
			annotations.add(new EmmyLuaAccess(modifier.getAccess()));
		}
		if (builder.hasVarArg)
		{
			if (!params.isEmpty())
			{
				// annotate last parameter as variadic argument
				for (int i = 0, size = params.size() - 1; i < size; i++) {
					annotations.addAll(params.get(i).getAnnotations());
				}
				annotations.add(new EmmyLuaVarArg(params.get(params.size() - 1).getType()));
			}
			else {
				builder.hasVarArg = false;
				Logger.error("Method %s marked with hasVarArg with no parameters", toString());
			}
		}
		else params.forEach(p -> annotations.addAll(p.getAnnotations()));

		annotations.add(new EmmyLuaReturn(returnType));
		this.annotations = Collections.unmodifiableList(annotations);
		this.hasVarArg = builder.hasVarArg;
		this.comment = builder.comment;
	}

	public void appendParameterSignature(StringBuilder sb) {

		if (params.size() > 0)
		{
			int lastElementIndex = params.size() - 1;
			// method has 2 or more parameters
			if (lastElementIndex > 0)
			{
				sb.append(params.get(0).getName());
				for (int i = 1; i < lastElementIndex; i++) {
					sb.append(", ").append(params.get(i).getName());
				}
				sb.append(", ");
			}
			if (!hasVarArg()) {
				sb.append(params.get(lastElementIndex).getName());
			}
			// method has variadic argument
			else sb.append("...");
		}
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		appendParameterSignature(sb);

		return String.format("%s%s(%s)", owner != null ?
				owner.getName() + ':' : "", getName(), sb.toString());
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
	public LuaType getReturnType() {
		return returnType;
	}

	@Override
	public @UnmodifiableView List<LuaParameter> getParams() {
		return params;
	}

	@Override
	public boolean hasVarArg() {
		return hasVarArg;
	}

	@Override
	public @Unmodifiable List<EmmyLua> getAnnotations() {
		return annotations;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LuaMethod luaMethod = (LuaMethod) o;

		if (!Objects.equals(owner, luaMethod.owner)) {
			return false;
		}
		if (!name.equals(luaMethod.name)) {
			return false;
		}
		if (!returnType.equals(luaMethod.returnType)) {
			return false;
		}
		if (!params.equals(luaMethod.params)) {
			return false;
		}
		if (hasVarArg != luaMethod.hasVarArg) {
			return false;
		}
		return modifier.equals(luaMethod.modifier);
	}

	@Override
	public int hashCode() {

		int result = owner != null ? owner.hashCode() : 0;
		result = 31 * result + name.hashCode();
		result = 31 * result + returnType.hashCode();
		result = 31 * result + params.hashCode();
		result = 31 * result + modifier.hashCode();
		return 31 * result + (hasVarArg ? 1 : 0);
	}

	public static class Builder {

		private final String name;

		private @Nullable LuaClass owner;
		private @Nullable MemberModifier modifier;
		private @Nullable LuaType returnType;

		private List<LuaParameter> params = new ArrayList<>();
		private boolean hasVarArg = false;
		private String comment = "";

		private Builder(String name) {
			this.name = name;
		}

		public static Builder create(String name) {
			return new Builder(name);
		}

		public Builder withOwner(LuaClass owner) {
			this.owner = owner;
			return this;
		}

		public Builder withModifier(MemberModifier modifier) {
			this.modifier = modifier;
			return this;
		}

		public Builder withReturnType(LuaType returnType) {
			this.returnType = returnType;
			return this;
		}

		public Builder withVarArg(boolean hasVarArg) {
			this.hasVarArg = hasVarArg;
			return this;
		}

		public Builder withComment(String comment) {
			this.comment = comment;
			return this;
		}

		public Builder withParams(List<LuaParameter> params) {
			this.params = params;
			return this;
		}

		public LuaMethod build() {
			return new LuaMethod(this);
		}
	}
}
