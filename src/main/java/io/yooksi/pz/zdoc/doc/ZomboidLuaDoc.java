/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
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
package io.yooksi.pz.zdoc.doc;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.Main;
import io.yooksi.pz.zdoc.compile.LuaCompiler;
import io.yooksi.pz.zdoc.element.IMember;
import io.yooksi.pz.zdoc.element.lua.Annotated;
import io.yooksi.pz.zdoc.element.lua.LuaClass;
import io.yooksi.pz.zdoc.element.lua.LuaField;
import io.yooksi.pz.zdoc.element.lua.LuaMethod;
import io.yooksi.pz.zdoc.lang.lua.EmmyLua;

public class ZomboidLuaDoc implements ZomboidDoc {

	private final LuaClass clazz;
	private final @UnmodifiableView List<LuaField> fields;
	private final @UnmodifiableView Set<LuaMethod> methods;

	public ZomboidLuaDoc(LuaClass clazz, List<LuaField> fields, Set<LuaMethod> methods) {
		this.clazz = clazz;
		this.fields = Collections.unmodifiableList(Validate.noNullElements(fields));
		this.methods = Collections.unmodifiableSet(Validate.noNullElements(methods));
	}

	public ZomboidLuaDoc(LuaClass clazz) {
		this(clazz, new ArrayList<>(), new HashSet<>());
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

	private static void appendComments(StringBuilder sb, IMember member) {

		String comment = member.getComment();
		if (!StringUtils.isBlank(comment))
		{
			String[] comments = comment.split("(\\r\\n|\\r|\\n)");
			int commentCount = comments.length;
			if (commentCount > 0)
			{
				sb.append("---").append(comments[0]);
				for (int i = 1; i < commentCount; i++) {
					sb.append("\n---\n---").append(comments[i]);
				}
				sb.append('\n');
			}
		}
	}

	public static void writeGlobalTypesToFile(File file) throws IOException {

		StringBuilder sb = new StringBuilder();
		for (LuaClass globalType : LuaCompiler.getGlobalTypes())
		{
			ZomboidLuaDoc.appendAnnotations(sb, globalType);
			sb.append(globalType.getConventionalName()).append(" = {}\n\n");
		}
		FileUtils.write(file, sb.toString(), Main.CHARSET, false);
	}

	public void writeToFile(File file) throws IOException {

		StringBuilder sb = new StringBuilder();

		ZomboidLuaDoc.appendAnnotations(sb, clazz);
		// TODO: static fields should be written as actual fields (not just annotations)
		for (LuaField field : fields)
		{
//			ZomboidLuaDoc.appendComments(sb, field);
			ZomboidLuaDoc.appendAnnotations(sb, field);
		}
		// TODO: newlines should be platform independent
		sb.append(clazz.getConventionalName()).append(" = {}\n\n");

		for (LuaMethod method : methods)
		{
			ZomboidLuaDoc.appendComments(sb, method);
			ZomboidLuaDoc.appendAnnotations(sb, method);

			sb.append("function ").append(clazz.getConventionalName());
			sb.append(':').append(method.getName()).append('(');

			method.appendParameterSignature(sb);
			sb.append(") end\n\n");
		}
		sb.deleteCharAt(sb.length() - 1);
		FileUtils.write(file, sb.toString(), Main.CHARSET, false);
	}

	@Override
	public LuaClass getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return clazz.getConventionalName();
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
