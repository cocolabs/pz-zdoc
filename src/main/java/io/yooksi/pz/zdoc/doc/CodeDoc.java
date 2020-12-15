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
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.UnmodifiableView;

import io.yooksi.pz.zdoc.element.MemberClass;
import io.yooksi.pz.zdoc.element.Method;
import io.yooksi.pz.zdoc.lang.ParseResult;

/**
 * This class represents a parsed code based document.
 */
@SuppressWarnings("unused")
public abstract class CodeDoc implements ParseResult {

	/** Name of this document and main class. */
	private final String name;

	/** Non-null guaranteed list of parsed methods contained in this document. */
	private final @UnmodifiableView List<? extends Method> methods;

	/** Textual representation of this document ready for output. */
	private final @UnmodifiableView List<String> content;

	/** Textual representation of class fields or function parameters. */
	private final @UnmodifiableView Map<String, MemberClass> members;

	public CodeDoc(String name, List<String> content,
				   Set<? extends MemberClass> members, List<? extends Method> methods) {

		this.name = name;
		this.content = Collections.unmodifiableList(content);
		this.methods = Collections.unmodifiableList(
				ListUtils.predicatedList(methods, PredicateUtils.notNullPredicate()));

		Map<String, MemberClass> membersMap = new java.util.HashMap<>();
		members.forEach(m -> membersMap.put(m.getName(), m));
		this.members = Collections.unmodifiableMap(MapUtils.predicatedMap(membersMap,
				PredicateUtils.notNullPredicate(), PredicateUtils.notNullPredicate()));
	}

	public String getName() {
		return name;
	}

	public List<String> readContent() {
		return content;
	}

	public Map<String, ? extends MemberClass> getMembers() {
		return members;
	}

	@SuppressWarnings("unchecked")
	public <T extends Method> List<T> getMethods() {
		return (List<T>) methods;
	}

	/**
	 * Write textual representation of this document to file.
	 * Note that the file will be cleared of all content before writing.
	 *
	 * @param path path to target file.
	 *
	 * @throws IOException if an I/O error occurred while writing to file.
	 */
	public void writeToFile(Path path) throws IOException {
		FileUtils.writeLines(path.toFile(), content, false);
	}
}
