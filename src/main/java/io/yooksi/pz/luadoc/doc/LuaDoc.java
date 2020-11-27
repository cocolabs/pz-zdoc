package io.yooksi.pz.luadoc.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import io.yooksi.pz.luadoc.element.LuaClass;
import io.yooksi.pz.luadoc.element.LuaMethod;
import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.EmmyLua;

public class LuaDoc extends CodeDoc<LuaMethod> {

	/** Matches a class initialized through {@code class:derive(..)} */
	private static final Pattern DERIVED_CLASS = Pattern.compile("=\\s*(\\w+):derive\\(");

	public LuaDoc(List<String> content, Set<LuaClass> members, List<LuaMethod> methods) {
		super(content, members, methods);
	}

	public static class Parser extends DataParser<LuaDoc, File> {

		private LuaDoc parseInternal() throws IOException {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			else if (!data.exists()) {
				throw new FileNotFoundException(data.getPath());
			}
			String filename = FilenameUtils.getBaseName(data.getName());

			List<String> content = new ArrayList<>();
			Set<LuaClass> members = new java.util.HashSet<>();

			List<String> input = FileUtils.readLines(data, Charset.defaultCharset());
			for (int i = 0; i < input.size(); i++)
			{
				String line = input.get(i);
				Pattern pattern = Pattern.compile("^\\s*" + filename + "\\s+=");
				if (pattern.matcher(line).find())
				{
					if (i > 0)
					{ // make sure we are not on the first line
						String prevLine = input.get(i - 1);
						if (EmmyLua.CLASS.isAnnotation(prevLine)) {
							content.remove(i - 1);
						}
					}
					String annotation = EmmyLua.CLASS.create(new String[]{ filename });
					Matcher matcher = DERIVED_CLASS.matcher(line);
					if (matcher.find())
					{
						String type = matcher.group(1);
						members.add(new LuaClass(filename, type));
						annotation += " : " + type;
					}
					else members.add(new LuaClass(filename));
					content.add(annotation);
				}
				content.add(line);
			}
			return new LuaDoc(content, members, new ArrayList<>());
		}

		@Override
		public LuaDoc parse() {

			try {
				return parseInternal();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
