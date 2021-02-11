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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.zdoc.element.SignatureToken;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.util.ParseUtils;
import io.yooksi.pz.zdoc.util.Utils;

abstract class SignatureParser<T extends SignatureToken> {

	final String signature;
	final StringBuilder builder;
	final AtomicInteger index;
	final List<T> result;

	SignatureParser(String signature, AtomicInteger index) {
		this.signature = signature;
		this.builder = new StringBuilder();
		this.result = new ArrayList<>();
		this.index = index;
	}

	SignatureParser(String signature) {
		this(signature, new AtomicInteger());
	}

	static @Nullable JavaClass getClassForName(String name) {
		try {
			return new JavaClass(Utils.getClassForName(name));
		}
		catch (ClassNotFoundException e) {
			Logger.debug("Failed to get class for name: " + name);
		}
		return null;
	}

	String flush() {
		return ParseUtils.flushStringBuilder(builder);
	}

	abstract List<T> parse() throws SignatureParsingException;
}
