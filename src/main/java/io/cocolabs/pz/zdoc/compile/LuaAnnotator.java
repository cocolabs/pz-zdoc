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
package io.cocolabs.pz.zdoc.compile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import io.cocolabs.pz.zdoc.Main;
import io.cocolabs.pz.zdoc.element.lua.LuaClass;
import io.cocolabs.pz.zdoc.lang.lua.EmmyLuaClass;
import io.cocolabs.pz.zdoc.logger.Logger;
import io.cocolabs.pz.zdoc.util.ParseUtils;

public class LuaAnnotator {

	/**
	 * <p>Matches a line containing lua table declaration.</p>
	 * <p>There are two capturing group that matches the table name and declaration type.</p>
	 * <blockquote>
	 * <p>Capturing groups:
	 * <ul>
	 * <li>group 1<sup>?</sup>: line indentation</li>
	 * <li>group 2<sup>+</sup>: table name</li>
	 * <li>group 3<sup>?</sup>: parent type name</li>
	 * <li>group 4<sup>?</sup>: declaration type <i>(new or derive)</i></li>
	 * </ul>
	 * </p>
	 * <p>Example:
	 * <pre>
	 * String table1 = "NewTestTable = ISTestTable:new(\"TestTable\")";
	 * String table2 = "DerivedTestTable = ISTestTable:derive(\"TestTable\")";
	 * String table3 = "DeclaredTestTable = {}";
	 * Matcher matcher = ZomboidLuaDoc.LUA_TABLE_DECLARATION.matcher(table1);
	 * assert matcher.find() {@code &&} matcher.group(2).equals("NewTestTable");
	 * assert matcher.group(3).equals("ISTestTable") {@code &&} matcher.group(4).equals("new");
	 * matcher = ZomboidLuaDoc.LUA_TABLE_DECLARATION.matcher(table2);
	 * assert matcher.find() {@code &&} matcher.group(2).equals("DerivedTestTable");
	 * assert matcher.group(3).equals("ISTestTable") {@code &&} matcher.group(4).equals("derive");
	 * matcher = ZomboidLuaDoc.LUA_TABLE_DECLARATION.matcher(table3);
	 * assert matcher.find() && matcher.group(2).equals("DeclaredTestTable");
	 * assert matcher.group(3) == null && matcher.group(4) == null
	 * </pre>
	 * </p>
	 * </blockquote>
	 */
	static final Pattern LUA_TABLE_DECLARATION = Pattern.compile(
			"^(\\s+)?(\\w+)\\s*=(?:\\s*([^\\s]+):(new|derive)\\(|.*\\s*).*$"
	);

	/**
	 * <p>Annotate Lua class representing the given file with {@link EmmyLuaClass} annotations.
	 * The method reads the file line by line and searches for a table declaration that matches
	 * the name of the given file and annotates it with an {@code EmmyLuaClass} annotation by
	 * inserting it one line above the declaration line. The entire content of the file
	 * alongside the annotation line is copied in returned {@code List}.</p>
	 * <p>This process is further defined with {@code AnnotateRules}:</p>
	 * <ul>
	 * <li>If {@code AnnotateRules} specifies inclusion rules for this file, the file is parsed
	 * until finding all matching table entries defined as rule property values.
	 * See <a href="../doc/ZomboidLuaDoc.AnnotateRules.html#include">Inclusion rules</a>.
	 * </li>
	 * <li>The file will <b>not</b> be annotated if the table name is contained in the
	 * {@code Set} of excluded classes in {@code AnnotateRules}, otherwise the {@code Set} will
	 * be mutated to include the matched table name to make it more convenient when working in loops.
	 * See <a href="../doc/ZomboidLuaDoc.AnnotateRules.html#exclude">Exclusion rules</a>.
	 * </li>
	 * </ul>
	 *
	 * @param file {@code File} to annotate the class for.
	 * @param content {@code List} to copy the annotated contents of given {@code File} line by line.
	 * 		Since the list is mutated in the annotation process, passing an <i>immutable</i> list
	 * 		implementation would throw an {@code UnsupportedOperationException}.
	 * @param rules rules to apply in the annotation process.
	 * @return {@code AnnotateResult} specifying the result of annotation process.
	 *
	 * @throws FileNotFoundException if the given {@code File} does not exist.
	 * @throws IOException if an I/O exception was thrown while reading file.
	 * @throws UnsupportedOperationException if content {@code List} or exclusion rules {@code Set}
	 * 		is an immutable {@code Collection} implementation.
	 */
	public static AnnotateResult annotate(File file, List<String> content, AnnotateRules rules) throws IOException {

		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		List<String> input = FileUtils.readLines(file, Main.CHARSET);
		if (input.size() == 0) {
			return AnnotateResult.SKIPPED_FILE_EMPTY;
		}
		Set<String> include = new HashSet<>();
		String tableName = FilenameUtils.removeExtension(file.getName());
		String includeValue = (String) rules.include.get(tableName);
		/*
		 * include rules are intended to override inferred main table,
		 * either use the supplied list of rules or table name
		 */
		if (includeValue != null)
		{
			if (!StringUtils.isBlank(includeValue)) {
				include.addAll(Arrays.asList(includeValue.split(",")));
			}
			// if property value is blank the file is meant to be ignored
			else return AnnotateResult.SKIPPED_FILE_IGNORED;
		}
		else include.add(tableName);

		final int includeCountMax = include.size();
		int includeCount = 0, excludeCount = 0;

		boolean foundNonBlankLine = false;  // true if file is not empty

		for (int i = 0; i < input.size(); i++)
		{
			String line = input.get(i);
			if (StringUtils.isBlank(line))
			{
				content.add(line);
				continue;
			}
			/* evaluate if file content is consistent of only blank lines,
			 * this could be done in stream but would be inefficient to
			 * deal with streams and iterate over file content twice
			 */
			else if (!foundNonBlankLine) {
				foundNonBlankLine = true;
			}
			/*
			 * skip regex matching if all elements were already annotated,
			 * just continue copying file content to list
			 */
			if (!include.isEmpty())
			{
				Matcher match = LUA_TABLE_DECLARATION.matcher(line);
				if (match.find())
				{
					String matchedName = match.group(2);
					if (include.contains(matchedName))
					{
						LuaClass luaClass = new LuaClass(matchedName, match.group(3));
						if (!rules.exclude.contains(luaClass.getName()))
						{
							// make sure we are not on the first line
							if (i > 0 && EmmyLuaClass.isAnnotation(input.get(i - 1))) {
								content.remove(i - 1);
							}
							/* take indentation in consideration just in case
							 * the table declaration is indented (should not normally be the case)
							 */
							String indentation = ParseUtils.getOptionalMatchedGroup(match, 1);
							content.add(indentation + luaClass.getAnnotations().get(0));
							rules.exclude.add(luaClass.getName());

							include.remove(matchedName);
							includeCount += 1;
						}
						else excludeCount += 1;
					}
				}
			}
			content.add(line);
		}
		Logger.debug(String.format("Annotation process finished - " +
				"included (%d/%d), excluded %d", includeCount, includeCountMax, excludeCount)
		);
		if (!foundNonBlankLine) {
			return AnnotateResult.SKIPPED_FILE_EMPTY;
		}
		else if (excludeCount == includeCountMax) {
			return AnnotateResult.ALL_EXCLUDED;
		}
		else if (includeCount == includeCountMax) {
			return AnnotateResult.ALL_INCLUDED;
		}
		else if (excludeCount == 0 && includeCount == 0) {
			return AnnotateResult.NO_MATCH;
		}
		else return AnnotateResult.PARTIAL_INCLUSION;
	}

	/**
	 * Annotate Lua class representing the given file with {@link EmmyLuaClass} annotation.
	 *
	 * @param file {@code File} to annotate the class for.
	 * @param content {@code List} to copy the annotated contents of given {@code File} line by line.
	 * @return {@code AnnotateResult} specifying the result of annotation process.
	 *
	 * @throws FileNotFoundException if the given {@code File} does not exist.
	 * @throws IOException if an I/O exception was thrown while reading file.
	 * @see #annotate(File, List, AnnotateRules)
	 */
	static AnnotateResult annotate(File file, List<String> content) throws IOException {
		return LuaAnnotator.annotate(file, content, new AnnotateRules());
	}

	public enum AnnotateResult {

		/** Indicates that annotation process was skipped because target file was empty. */
		SKIPPED_FILE_EMPTY,

		/** Indicated that annotation process was skipped because target file is ignored. */
		SKIPPED_FILE_IGNORED,

		/** Indicates that all annotation elements were excluded. */
		ALL_EXCLUDED,

		/** Indicates that all annotation elements were included. */
		ALL_INCLUDED,

		/** Indicates that some elements were excluded. */
		PARTIAL_INCLUSION,

		/** Indicates that no elements were matched. */
		NO_MATCH
	}

	/**
	 * This class represents annotation process rules.
	 * <h3><a id="include">Include entries</a></h3>
	 * {@code Properties} that hold inclusion rules for annotation process.
	 * <ul>
	 *     <li>Left-hand side (key) represents filenames.</li>
	 *     <li>Right-hand side (value) represents class names to include.
	 *     <ul>
	 *         <li>Side accepts single and multiple name entries.</li>
	 *         <li>Multiple entries are separated with comma delimiter.</li>
	 *         <li>Blank entry instructs the process to skip the entry.</li>
	 *     </ul></li>
	 * </ul>
	 * <h2><a id="exclude">Exclude entries</a></h2>
	 * <p>{@code Set} that holds exclusion rules for annotation process.</p>
	 * <ul>
	 *     <li>Entries here represent class names to exclude.</li>
	 *     <li>{@code Set} is mutated in the annotation process so initializing this variable
	 *     with an Immutable {@code Set} will result in a {@code UnsupportedOperationsException}.
	 *     </li>
	 * </ul>
	 *
	 * @see #annotate(File, List, AnnotateRules)
	 */
	public static class AnnotateRules {

		private final Properties include;
		private final Set<String> exclude;

		public AnnotateRules(Properties include, Set<String> exclude) {
			this.include = include;
			this.exclude = exclude;
		}

		AnnotateRules(Properties include) {
			this(include, new HashSet<>());
		}

		AnnotateRules(Set<String> exclude) {
			this(new Properties(), exclude);
		}

		AnnotateRules() {
			this.include = new Properties();
			this.exclude = new HashSet<>();
		}
	}
}
