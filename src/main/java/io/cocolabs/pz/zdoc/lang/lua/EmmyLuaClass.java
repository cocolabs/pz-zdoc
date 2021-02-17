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
package io.cocolabs.pz.zdoc.lang.lua;

import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

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

	public EmmyLuaClass(String type, @Nullable String parentType, String comment) {
		super("class", formatType(type, parentType), comment);
	}

	public EmmyLuaClass(String type, @Nullable String parentType) {
		this(type, parentType, "");
	}

	public static boolean isAnnotation(String text) {
		return REGEX.matcher(text).find();
	}

	private static String formatType(String type, @Nullable String parentType) {
		return parentType != null ? type + " : " + parentType : type;
	}
}
