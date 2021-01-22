/*
 * Copyright (C) 2020 Matthew Cain
 * ZomboidDoc - Lua library compiler for Project Zomboid
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

import java.lang.reflect.Field;

import io.yooksi.pz.zdoc.element.IField;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;

/**
 * This class represents a wrapped {@link Field} object.
 */
public class JavaField implements IField {

	private final String name;
	private final JavaClass type;
	private final MemberModifier modifier;
	private final String comment;

	public JavaField(JavaClass type, String name, MemberModifier modifier, String comment) {
		this.name = name;
		this.type = type;
		this.modifier = modifier;
		this.comment = comment;
	}

	public JavaField(JavaClass type, String name, MemberModifier modifier) {
		this(type, name, modifier, "");
	}

	public JavaField(Class<?> type, String name, MemberModifier modifier) {
		this(new JavaClass(type), name, modifier, "");
	}

	public JavaField(Field field) {
		this.name = field.getName();
		this.type = new JavaClass(field.getType());
		this.modifier = new MemberModifier(field.getModifiers());
		this.comment = "";
	}

	@Override
	public String toString() {
		return (modifier.toString() + ' ' + type.getName() + ' ' + getName()).trim();
	}

	@Override
	public JavaClass getType() {
		return type;
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

	public boolean equals(JavaField field, boolean shallow) {

		if (shallow)
		{
			if (this == field) {
				return true;
			}
			if (field == null) {
				return false;
			}
			if (!name.equals(field.name)) {
				return false;
			}
			if (!type.equals(field.type, true)) {
				return false;
			}
			return modifier.equals(field.modifier);
		}
		else return equals(field);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JavaField jField = (JavaField) obj;
		if (name.equals(jField.name) && type.equals(jField.type)) {
			return true;
		}
		return modifier.equals(jField.modifier);
	}

	@Override
	public int hashCode() {

		int result = 31 * name.hashCode() + type.hashCode();
		return 31 * result + modifier.hashCode();
	}
}
