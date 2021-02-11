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

import java.util.List;

import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.util.Utils;

public class MethodSignatureParser extends SignatureParser<JavaParameter> {

	private boolean isVarArg = false;

	MethodSignatureParser(String signature) {
		super(signature);
	}

	@Override
	List<JavaParameter> parse() throws SignatureParsingException {

		JavaClass type = null;
		char[] charArray = signature.toCharArray();
		for (; index.get() < charArray.length; index.getAndIncrement())
		{
			char c = charArray[index.get()];
			if (c == '<')
			{
				index.incrementAndGet();
				String className = flush();
				try {
					Class<?> typeClass = Utils.getClassForName(className);
					type = new JavaClass(typeClass, new TypeSignatureParser(signature, index).parse());
				}
				catch (ClassNotFoundException e) {
					throwExceptionUnknownClass(className);
				}
			}
			else if (c == ',')
			{
				if (type == null) {
					throw new MalformedSignatureException(signature, "expected type to not be null");
				}
				result.add(new JavaParameter(type, flush()));
			}
			else if (c == ' ')
			{
				if (type == null || builder.length() != 0)
				{
					String className = flush();
					try {
						type = new JavaClass(Utils.getClassForName(className));
					}
					catch (ClassNotFoundException e1)
					{
						// parameter is a variadic argument
						if (className.endsWith("..."))
						{
							// skip if builder string was already consumed by type
							if (className.length() != 3)
							{
								try {
									className = className.substring(0, className.length() - 3);
									type = new JavaClass(Utils.getClassForName(className));
								}
								catch (ClassNotFoundException e2) {
									throwExceptionUnknownClass(className);
								}
							}
							isVarArg = true;
						}
						else throwExceptionUnknownClass(className);
					}
				}
			}
			else if (c != '>') {
				builder.append(c);
			}
		}
		if (builder.length() > 0)
		{
			String param = flush();
			if (type == null)
			{
				String message = String.format("Parameter \"%s\" is missing type", param);
				throw new MalformedSignatureException(signature, message);
			}
			result.add(new JavaParameter(type, param));
		}
		return result;
	}

	boolean isVarArg() {
		return isVarArg;
	}

	private void throwExceptionUnknownClass(String className) throws SignatureParsingException {
		throw new SignatureParsingException(signature, "unknown class: " + className);
	}
}
