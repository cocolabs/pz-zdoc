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
package io.yooksi.pz.zdoc.lang.lua;

import io.yooksi.pz.zdoc.element.lua.LuaClass;

import java.util.regex.Pattern;

/**
 * EmmyLua use {@code @class} to simulate classes in OOP, supporting inheritance and fields.
 * <ul>
 * <li>Full format:<pre>
 * --@class MY_TYPE[:PARENT_TYPE] [@comment]
 * </pre></li>
 * <li>Target:</li>
 * <li><ul style="list-style-type:circle">
 * <li>local variables</li>
 * <li>global variables</li>
 * </ul></li>
 * <li>Examples:<pre>
 * ---@class Car : Transport @define class Car extends Transport
 * local cls = class()
 * function cls:test()
 * end
 * </pre></li>
 * </ul>
 *
 * @see <a href="https://git.io/JLPlJ">EmmyLua Documentation</a>
 */
public class EmmyLuaClass extends EmmyLua {

	private static final Pattern REGEX = Pattern.compile(
			"^---\\s*@class\\s+(\\w+)(?:\\s*:\\s*(\\w+))?(?:\\s*@\\s*(.*))?\\s*$?"
	);

	public EmmyLuaClass(LuaClass type, String comment) {
		super("class", formatType(type), comment);
	}

	public EmmyLuaClass(LuaClass type) {
		this(type, "");
	}

	public static boolean isAnnotation(String text) {
		return REGEX.matcher(text).find();
	}

	private static String formatType(LuaClass type) {

		String sType = type.getName();
		String pType = type.getParentType();
		return pType != null ? sType + " : " + pType : sType;
	}
}
