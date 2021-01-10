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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.yooksi.pz.zdoc.Main;
import io.yooksi.pz.zdoc.util.Utils;

/**
 * This class represents a parsed JavaDoc document.
 */
public class ZomboidAPIDoc {

	private static final URL API_URL = Utils.getURL("https://projectzomboid.com/modding");

	private final Document document;
	private final String name;

	private ZomboidAPIDoc(Document document, String name) {
		this.document = document;
		this.name = name;
	}

	/**
	 * Get Project Zomboid API page from resolved {@code URL} for given path.
	 *
	 * @param path path to API page <i>(does not need to have an extension)</i>.
	 * @return new {@code ZomboidDoc} instance wrapping a parsed {@code HTML} document
	 * 		representing API page for given path, or {@code null} if the API website
	 * 		returned status code {@code 404} (page not found).
	 *
	 * @throws IOException if {@link Jsoup} encountered an error while executing GET request.
	 */
	public static @Nullable ZomboidAPIDoc getPage(Path path) throws IOException {

		try {
			String apiUrl = resolveURL(path.toString().replace('\\', '/')).toString();
			return new ZomboidAPIDoc(Jsoup.connect(apiUrl).get(), path.getFileName().toString());
		}
		catch (IOException e)
		{
			if (e instanceof HttpStatusException && ((HttpStatusException) e).getStatusCode() == 404) {
				return null;
			}
			else throw e;
		}
	}

	/**
	 * Get Project Zomboid API page for given path.
	 *
	 * @param path path to API page in local file system.
	 * @return new {@code ZomboidDoc} instance wrapping a parsed {@code HTML}
	 * 		document representing local API page for given path.
	 *
	 * @throws IOException if {@link Jsoup} could not find the API document.
	 */
	static ZomboidAPIDoc getLocalPage(Path path) throws IOException {
		return new ZomboidAPIDoc(Jsoup.parse(path.toFile(), Main.CHARSET), path.getFileName().toString());
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
	public static URL resolveURL(String path) {

		URL url = Utils.getURLOrNull(path);
		if (url != null)
		{
			if (!isValidURL(url)) {
				throw new IllegalArgumentException("Invalid modding API url: " + url.toString());
			}
			else return url;
		}
		Path pPath = Utils.getPathOrNull(path);
		if (pPath != null)
		{
			String ext = FilenameUtils.getExtension(pPath.getFileName().toString());
			return Utils.getURL(API_URL, "modding", ext.equals("html") ? path : path + ".html");
		}
		else throw new IllegalArgumentException(String.format("Cannot resolve api URL - " +
				"argument \"%s\" is not a valid Path or URL", path));
	}

	public static Path resolveURLPath(String url) {

		URL uUrl = Utils.getURLOrNull(url);
		if (uUrl != null && isValidURL(uUrl)) {
			return Paths.get("/modding").relativize(Paths.get(uUrl.getPath()));
		}
		else throw new IllegalArgumentException(String.format("Cannot resolve API URL path, " +
				"argument \"%s\" is not a valid URL object", url));
	}

	public static boolean isValidURL(URL url) {

		Path urlPath = Paths.get(url.getPath());
		URL host = Utils.getURLBase(url);

		URL segment, target;
		if (urlPath.getNameCount() > 0)
		{
			segment = Utils.getURL(host, urlPath.getName(0).toString());
			target = API_URL;
		}
		else {
			segment = host;
			target = Utils.getURLBase(API_URL);
		}
		try {
			// use URI comparison because URL equivalent is flawed
			if (!segment.toURI().equals(target.toURI())) {
				return false;
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	public Document getDocument() {
		return document;
	}

	public String getName() {
		return name;
	}
}
