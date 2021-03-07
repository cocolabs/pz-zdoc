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
package io.cocolabs.pz.zdoc.doc;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.UnmodifiableView;

import com.google.common.base.Splitter;

import io.cocolabs.pz.zdoc.Main;
import io.cocolabs.pz.zdoc.compile.JavaCompiler;
import io.cocolabs.pz.zdoc.compile.LuaCompiler;
import io.cocolabs.pz.zdoc.element.IMember;
import io.cocolabs.pz.zdoc.element.lua.Annotated;
import io.cocolabs.pz.zdoc.element.lua.LuaClass;
import io.cocolabs.pz.zdoc.element.lua.LuaField;
import io.cocolabs.pz.zdoc.element.lua.LuaMethod;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLua;
import io.cocolabs.pz.zdoc.logger.Logger;

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
			List<String> comments = Splitter.onPattern("(\\r\\n|\\r|\\n)").splitToList(comment);
			int commentCount = comments.size();
			if (commentCount > 0)
			{
				sb.append("---").append(comments.get(0).trim());
				for (int i = 1; i < commentCount; i++) {
					sb.append("\n---\n---").append(comments.get(i).trim());
				}
				sb.append('\n');
			}
		}
	}

	public static void writeGlobalTypesToFile(File file) throws IOException {

		Logger.detail("Writing global lua types to file...");
		StringBuilder sb = new StringBuilder();
		Set<LuaClass> globalTypes = LuaCompiler.getGlobalTypes();
		for (LuaClass type : globalTypes)
		{
			ZomboidLuaDoc.appendAnnotations(sb, type);
			sb.append(type.getConventionalName()).append(" = {}\n\n");
		}
		FileUtils.write(file, sb.toString(), Main.CHARSET, false);
		Logger.info("Compiled %d global lua types", globalTypes.size());
	}

	public void writeToFile(File file) throws IOException {

		Logger.detail("Writing %s to %s...", getName(), file.getName());
		StringBuilder sb = new StringBuilder();

		ZomboidLuaDoc.appendAnnotations(sb, clazz);
		// TODO: static fields should be written as actual fields (not just annotations)
		for (LuaField field : fields) {
			ZomboidLuaDoc.appendAnnotations(sb, field);
		}
		sb.append(clazz.getConventionalName()).append(" = {}\n\n");

		for (LuaMethod method : methods)
		{
			ZomboidLuaDoc.appendComments(sb, method);
			ZomboidLuaDoc.appendAnnotations(sb, method);

			sb.append("function ");

			// global methods need to be declared outside tables
			String parentType = clazz.getParentType();
			if (parentType == null || !parentType.equals(JavaCompiler.GLOBAL_OBJECT_CLASS)) {
				sb.append(clazz.getConventionalName()).append(':');
			}
			sb.append(method.getName()).append('(');
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
