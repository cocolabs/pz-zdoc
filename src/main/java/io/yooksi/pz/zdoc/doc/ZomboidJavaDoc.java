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
package io.yooksi.pz.zdoc.doc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaField;
import io.yooksi.pz.zdoc.element.java.JavaMethod;

public class ZomboidJavaDoc implements ZomboidDoc {

	private final JavaClass clazz;
	private final @UnmodifiableView List<JavaField> fields;
	private final @UnmodifiableView Set<JavaMethod> methods;

	public ZomboidJavaDoc(JavaClass clazz, List<JavaField> fields, Set<JavaMethod> methods) {
		this.clazz = clazz;
		this.fields = Collections.unmodifiableList(Validate.noNullElements(fields));
		this.methods = Collections.unmodifiableSet(Validate.noNullElements(methods));
	}

	@Override
	public JavaClass getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return clazz.getName();
	}

	@Override
	public @UnmodifiableView List<JavaField> getFields() {
		return fields;
	}

	@Override
	public @UnmodifiableView Set<JavaMethod> getMethods() {
		return methods;
	}
}
