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
package io.yooksi.pz.zdoc.doc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.yooksi.pz.zdoc.element.LuaClass;
import io.yooksi.pz.zdoc.element.LuaMethod;

public class LuaDoc extends CodeDoc<LuaMethod> {

	public LuaDoc(String name, List<String> content, Set<LuaClass> members, List<LuaMethod> methods) {
		super(name, content, members, methods);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, LuaClass> getMembers() {
		return (Map<String, LuaClass>) super.getMembers();
	}
}
