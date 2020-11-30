package io.yooksi.pz.luadoc.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.element.LuaClass;
import io.yooksi.pz.luadoc.element.LuaMethod;
import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.EmmyLua;
import io.yooksi.pz.luadoc.lang.ParseRegex;

public class LuaDoc extends CodeDoc<LuaMethod> {

	/** Matches a class initialized through {@code class:derive(..)} */
	private static final Pattern DERIVED_CLASS = Pattern.compile("=\\s*(\\w+):derive\\(");

	public LuaDoc(String name, List<String> content, Set<LuaClass> members, List<LuaMethod> methods) {
		super(name, content, members, methods);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, LuaClass> getMembers() {
		return (Map<String, LuaClass>) super.getMembers();
	}

	public static class Parser extends DataParser<LuaDoc, File> {

		private final Set<String> excludedMembers;

		private Parser(File data, Set<String> excludedMembers) {
			super(data);
			this.excludedMembers = excludedMembers;
		}

		public static Parser create(File data) {
			return new Parser(data, new HashSet<>());
		}

		public static Parser create(File data, Set<String> excludedMembers) {
			return new Parser(data, excludedMembers);
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
						String annotation = EmmyLua.CLASS.create(new String[]{ tableName });

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
}
