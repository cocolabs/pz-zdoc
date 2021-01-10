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

import io.yooksi.pz.zdoc.Main;
import io.yooksi.pz.zdoc.element.IClass;
import io.yooksi.pz.zdoc.element.lua.*;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ZomboidLuaDoc implements ZomboidDoc {

	protected final LuaClass clazz;
	protected final @UnmodifiableView List<LuaField> fields;
	protected final @UnmodifiableView Set<LuaMethod> methods;

	public ZomboidLuaDoc(LuaClass clazz, List<LuaField> fields, Set<LuaMethod> methods) {
		this.clazz = clazz;
		this.fields = Collections.unmodifiableList(Validate.noNullElements(fields));
		this.methods = Collections.unmodifiableSet(Validate.noNullElements(methods));
	}

	public ZomboidLuaDoc(LuaClass clazz) {
		this(clazz, new ArrayList<>(), new HashSet<>());
	}

	public void writeToFile(File file) throws IOException {

		StringBuilder sb = new StringBuilder();

		ZomboidLuaDoc.appendAnnotations(sb, clazz);
		fields.forEach(f -> ZomboidLuaDoc.appendAnnotations(sb, f));

		sb.append(clazz.getName()).append(" = {}\n\n");

		for (LuaMethod method : methods)
		{
			ZomboidLuaDoc.appendAnnotations(sb, method);

			sb.append("function ").append(clazz.getName());
			sb.append(':').append(method.getName()).append('(');

			List<LuaParameter> methodParams = method.getParams();
			if (methodParams.size() > 0)
			{
				sb.append(methodParams.get(0).getName());
				for (int i = 1; i < methodParams.size(); i++) {
					sb.append(", ").append(methodParams.get(i).getName());
				}
			}
			sb.append(") end\n\n");
		}
		sb.deleteCharAt(sb.length() - 1);
		FileUtils.write(file, sb.toString(), Main.CHARSET);
	}

	private static void appendAnnotations(StringBuilder sb, Annotated element) {

		List<EmmyLua> annotations = element.getAnnotations();
		if (annotations.size() > 0)
		{
			sb.append(annotations.get(0).toString());
			for (int i = 1; i < annotations.size(); i++) {
				sb.append('\n').append(annotations.get(i).toString());
			}
			sb.append('\n');
		}
	}

	@Override
	public IClass getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return clazz.getName();
	}

	@Override
	public @UnmodifiableView List<LuaField> getFields() {
		return fields;
	}

	@Override
	public @UnmodifiableView Set<LuaMethod> getMethods() {
		return methods;
	}
}
