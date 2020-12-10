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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.yooksi.pz.zdoc.Utils;
import io.yooksi.pz.zdoc.element.JavaClass;
import io.yooksi.pz.zdoc.element.JavaMethod;
import io.yooksi.pz.zdoc.element.LuaClass;
import io.yooksi.pz.zdoc.element.LuaMethod;

/**
 * This class represents a parsed JavaDoc document.
 */
public class JavaDoc<L> extends CodeDoc<JavaMethod> {

	private static final URL PZ_MODDING_URL = Utils.getURL("https://projectzomboid.com/modding");
	public static final URL API_GLOBAL_OBJECT = resolveApiURL("zombie/Lua/LuaManager.GlobalObject.html");

	public JavaDoc(String name, Set<JavaClass<L>> members, List<JavaMethod> methods) {
		super(name, new ArrayList<>(), members, methods);
	}

	public static URL resolveApiURL(String path) {

		URL url = Utils.getURLOrNull(path);
		if (url != null)
		{
			URL host = Utils.getURL(url.getProtocol() + "://" + url.getHost());
			URL segment = Utils.getURL(host, Paths.get(url.getPath()).getName(0).toString());
			try {
				if (!segment.toURI().equals(PZ_MODDING_URL.toURI())) {
					throw new IllegalArgumentException("Invalid PZ modding API url: " + segment.toString());
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			return url;
		}
		else if (Utils.isValidPath(path)) {
			return Utils.getURL(PZ_MODDING_URL, "modding", path);
		}
		else throw new IllegalArgumentException(String.format("Cannot resolve api URL - " +
					"argument \"%s\" is not a valid Path or URL", path));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, JavaClass<L>> getMembers() {
		return (Map<String, JavaClass<L>>) super.getMembers();
	}

	public LuaDoc compileLuaLibrary(boolean annotate, boolean qualify) {

		List<String> content = new java.util.ArrayList<>();
		List<LuaMethod> luaMethods = new java.util.ArrayList<>();

		if (qualify) {
			new LuaClass(getName()).writeTo(content, annotate);
		}
		List<JavaMethod> javaMethods = getMethods();
		for (JavaMethod method : javaMethods)
		{
			LuaMethod luaMethod = !qualify ? LuaMethod.Parser.create(method).parse()
					: LuaMethod.Parser.create(method, getName()).parse();

			if (annotate) {
				luaMethod.annotate();
			}
			luaMethods.add(luaMethod);
			content.add(luaMethod.toString());
			content.add("");
		}
		content.remove(content.size() - 1);
		return new LuaDoc(getName(), content, new java.util.HashSet<>(), luaMethods);
	}
}
