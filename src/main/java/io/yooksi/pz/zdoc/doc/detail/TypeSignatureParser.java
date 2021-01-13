/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.yooksi.pz.zdoc.element.java.JavaClass;
import org.jetbrains.annotations.Nullable;

class TypeSignatureParser extends SignatureParser<JavaClass> {

	private TypeSignatureParser(String signature) {
		super(signature);
	}

	private TypeSignatureParser(String signature, AtomicInteger index) {
		super(signature, index);
	}

	public static @Nullable JavaClass parse(String signature) {

		List<JavaClass> result = new TypeSignatureParser(signature).parse();
		return !result.isEmpty() ? result.get(0) : null;
	}

	@Override
	List<JavaClass> parse() {

		char[] charArray = signature.toCharArray();
		for (; index.get() < charArray.length; index.getAndIncrement())
		{
			char c = charArray[index.get()];
			if (c == '<')
			{
				JavaClass type = getClassForName(flush());
				if (type == null) {
					return result;
				}
				index.incrementAndGet();
				List<JavaClass> params = new TypeSignatureParser(signature, index).parse();
				result.add(new JavaClass(type.getClazz(), params));
			}
			else if (c == ',') {
				flushToResult();
			}
			else if (c == '>') {
				return flushToResult();
			}
			else if (c != ' ') {
				builder.append(c);
			}
		}
		if (result.isEmpty() && builder.length() > 0) {
			result.add(getClassForName(builder.toString()));
		}
		return result;
	}

	@Override
	List<JavaClass> flushToResult() {

		String name = flush();
		if (!name.isEmpty()) {
			result.add(getClassForName(name));
		}
		return result;
	}
}
