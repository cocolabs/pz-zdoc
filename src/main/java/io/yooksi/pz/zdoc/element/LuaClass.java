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

import java.util.Collection;

import io.yooksi.pz.zdoc.lang.EmmyLua;

/**
 * This class represents a parsed lua class reference.
 */
public class LuaClass implements MemberClass {

	private final String name;
	private final String type;

	public LuaClass(String name, String type) {
		this.name = name;
		// ensure built-in types are lower-cased
		this.type = EmmyLua.getSafeType(type);
	}

	public LuaClass(String name) {
		this.name = name;
		this.type = "";
	}

	public void writeTo(Collection<String> content, boolean annotate) {

		if (annotate)
		{
			content.add(EmmyLua.CLASS.create(type.isEmpty() ?
					new String[]{ name } : new String[]{ name, type }));
		}
		content.add(name + " = {}");
		content.add("");
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type);
	}

	@Override
	public String getName() {
		return name;
	}
}
