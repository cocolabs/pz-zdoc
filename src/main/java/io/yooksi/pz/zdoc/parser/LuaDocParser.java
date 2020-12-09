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
package io.yooksi.pz.zdoc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.zdoc.doc.LuaDoc;
import io.yooksi.pz.zdoc.element.LuaClass;
import io.yooksi.pz.zdoc.lang.DataParser;
import io.yooksi.pz.zdoc.lang.EmmyLua;
import io.yooksi.pz.zdoc.lang.ParseRegex;

public class LuaDocParser extends DataParser<LuaDoc, File> {

	/** Matches a class initialized through {@code class:derive(..)} */
	private static final Pattern DERIVED_CLASS = Pattern.compile("=\\s*(\\w+):derive\\(");

	private final Set<String> excludedMembers;

	private LuaDocParser(File data, Set<String> excludedMembers) {
		super(data);
		this.excludedMembers = excludedMembers;
	}

	public static LuaDocParser create(File data) {
		return new LuaDocParser(data, new HashSet<>());
	}

	public static LuaDocParser create(File data, Set<String> excludedMembers) {
		return new LuaDocParser(data, excludedMembers);
	}

	@Override
	public LuaDoc parse() {

		if (data == null) {
			throw new RuntimeException("Tried to parse null data");
		}
		else if (!data.exists()) {
			throw new RuntimeException(new FileNotFoundException(data.getPath()));
		}
		List<String> content = new ArrayList<>();
		Set<LuaClass> members = new java.util.HashSet<>();

		List<String> input;
		try {
			input = FileUtils.readLines(data, Charset.defaultCharset());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < input.size(); i++)
		{
			String line = input.get(i);
			Matcher match = ParseRegex.LUA_TABLE_DECLARATION_REGEX.matcher(line);
			if (match.find())
			{
				@Nullable String indentation = match.group(1);
				String tableName = match.group(2);

				if (i > 0)
				{ // make sure we are not on the first line
					String prevLine = input.get(i - 1);
					if (EmmyLua.CLASS.isAnnotation(prevLine)) {
						content.remove(i - 1);
					}
				}
				if (!excludedMembers.contains(tableName))
				{
					LuaClass addedMember;
					String annotation = (indentation != null ? indentation : "") +
							EmmyLua.CLASS.create(new String[]{ tableName });

					Matcher matcher = DERIVED_CLASS.matcher(line);
					if (matcher.find())
					{
						String type = matcher.group(1);
						addedMember = new LuaClass(tableName, type);
						annotation += " : " + type;
					}
					else addedMember = new LuaClass(tableName);
					excludedMembers.add(addedMember.getName());

					members.add(addedMember);
					content.add(annotation);
				}
			}
			content.add(line);
		}
		return new LuaDoc(data.getName(), content, members, new ArrayList<>());
	}
}
