package io.yooksi.pz.luadoc.element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import io.yooksi.pz.luadoc.doc.JavaDoc;
import io.yooksi.pz.luadoc.lang.EmmyLua;
import io.yooksi.pz.luadoc.lang.ParseRegex;

/**
 * This class represents a parsed lua class reference.
 */
public class LuaClass extends MemberClass {

	private final String type;

	public LuaClass(String name, String type) {
		super(name);

		// ensure built-in types are lower-cased
		this.type = EmmyLua.getSafeType(type);
	}

	public LuaClass(String name) {
		super(name);
		this.type = "";
	}

	public Collection<String> writeTo(Collection<String> content, boolean annotate) {

		if (annotate)
		{
			content.add(EmmyLua.CLASS.create(type.isEmpty() ?
					new String[]{ name } : new String[]{ name, type }));
		}
		content.add(name + " = {}");
		content.add("");
		return content;
	}

	public static List<String> documentMembers(List<Method> methods, Set<String> excluded) {

		List<String> documentation = new ArrayList<>();
		Map<String, LuaClass> memberMap = new HashMap<>();

		for (Method method : methods)
		{
			String returnType = method.getReturnType(false);
			memberMap.putIfAbsent(returnType, new LuaClass(returnType));

			for (Parameter param : method.getParams())
			{
				String paramType = param.getType(false);
				memberMap.putIfAbsent(paramType, new LuaClass(paramType));
			}
		}
		Map<String, LuaClass> contentMap = new ConcurrentHashMap<>();
		for (Map.Entry<String, LuaClass> entry : memberMap.entrySet())
		{
			String key = entry.getKey();
			LuaClass value = entry.getValue();

			// remove array brackets
			String parsedKey = key.replaceAll("\\[\\s*]", "");
			if (EmmyLua.isBuiltInType(parsedKey)) {
				continue;
			}
			LuaClass luaClass = parsedKey.equals(key) ? value :
					new LuaClass(parsedKey, value.getType());

			boolean hasMatchedObjectType = false;
			Matcher match2 = ParseRegex.OBJECT_TYPE_REGEX.matcher(key);
			while (match2.find())
			{
				hasMatchedObjectType = true;
				for (String e : match2.group(1).split(","))
				{
					String s = JavaDoc.Parser.removeElementQualifier(e);
					if (!excluded.contains(s)) {
						contentMap.putIfAbsent(s, new LuaClass(s));
					}
				}
			}
			if (!hasMatchedObjectType && !excluded.contains(parsedKey)) {
				contentMap.putIfAbsent(parsedKey, luaClass);
			}
		}
		contentMap.forEach((n, m) -> m.writeTo(documentation));
		return documentation;
	}

	public Collection<String> writeTo(Collection<String> content) {
		return writeTo(content, true);
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type);
	}
}
