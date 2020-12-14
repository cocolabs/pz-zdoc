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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import io.yooksi.pz.zdoc.Utils;
import io.yooksi.pz.zdoc.element.JavaClass;
import io.yooksi.pz.zdoc.element.JavaMethod;
import io.yooksi.pz.zdoc.element.LuaClass;
import io.yooksi.pz.zdoc.element.LuaMethod;

/**
 * This class represents a parsed JavaDoc document.
 *
 * @param <L> object type denoting document location.
 * 		Use {@code Path} for local documents and {@code URL} for remote documents.
 */
public class JavaDoc<L> extends CodeDoc<JavaMethod> {

	private static final URL PZ_MODDING_URL = Utils.getURL("https://projectzomboid.com/modding");
	public static final URL API_GLOBAL_OBJECT = resolveApiURL("zombie/Lua/LuaManager.GlobalObject.html");

	public JavaDoc(String name, Set<JavaClass<L>> members, List<JavaMethod> methods) {
		super(name, new ArrayList<>(), members, methods);
	}

	/**
	 * <p>
	 * Resolve API url from the given path. If the given path can be parsed as an {@code URL}
	 * the parsed {@code URL} object will be returned, otherwise if the path represents a
	 * local file path it will be concatenated to the end of modding API URL.
	 * </p><br/>
	 * <i>
	 * Note that the resolved API URL always has to point to a HTML document,
	 * which means that the resulting URL is guaranteed to point to a file with
	 * an {@code .html} extension even if the user did not specify it.
	 * </i>
	 *
	 * @throws IllegalArgumentException if the given path is not a valid {@code Path}
	 * 		or {@code URL} object or if given path represents an {@code URL} object and
	 * 		is not a valid Project Zomboid modding API link.
	 */
	public static URL resolveApiURL(String path) {

		URL url = Utils.getURLOrNull(path);
		if (url != null)
		{
			/* validate if link is valid PZ modding API link,
			 * and use URI comparison because URL equivalent is flawed.
			 */
			URL host = Utils.getURL(url.getProtocol() + "://" + url.getHost());
			Path urlPath = Paths.get(url.getPath());
			if (urlPath.getNameCount() > 0)
			{
				URL segment = Utils.getURL(host, urlPath.getName(0).toString());
				try {
					if (!segment.toURI().equals(PZ_MODDING_URL.toURI())) {
						throw new IllegalArgumentException("Invalid PZ modding API url: " + segment.toString());
					}
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
			return url;
		}
		Path pPath = Utils.getPathOrNull(path);
		if (pPath != null)
		{
			String ext = FilenameUtils.getExtension(pPath.getFileName().toString());
			return Utils.getURL(PZ_MODDING_URL, "modding", ext.equals("html") ? path : path + ".html");
		}
		else throw new IllegalArgumentException(String.format("Cannot resolve api URL - " +
				"argument \"%s\" is not a valid Path or URL", path));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, JavaClass<L>> getMembers() {
		return (Map<String, JavaClass<L>>) super.getMembers();
	}

	/**
	 * Compiles Lua library from Java documentation.
	 *
	 * @param annotate should annotate elements with EmmyLua.
	 * @param qualify should compile members with qualifiers.
	 * @return compiled Lua library document.
	 */
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
		// remove empty line at end of file
		content.remove(content.size() - 1);
		return new LuaDoc(getName(), content, new java.util.HashSet<>(), luaMethods);
	}
}
