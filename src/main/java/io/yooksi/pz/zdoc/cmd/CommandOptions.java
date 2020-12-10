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
package io.yooksi.pz.zdoc.cmd;

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

public final class CommandOptions {

	static final Option INPUT_OPTION =
			Option.builder("i").longOpt("input-path").desc("input directory path")
					.type(File.class).required(true).hasArg().argName("path")
					.valueSeparator(' ').build();

	static final Option OUTPUT_OPTION =
			Option.builder("o").longOpt("output-path").desc("output directory path")
					.type(File.class).required(false).hasArg().argName("path")
					.valueSeparator(' ').build();

	static final Option API_OPTION =
			Option.builder("a").longOpt("api-doc").desc("read api from url or path")
					.type(String.class).required(false).hasArg().optionalArg(true)
					.argName("url|path").valueSeparator(' ').build();

	static final Option INCLUDE_REFS_OPTION =
			Option.builder("r").longOpt("include-refs").desc("include referenced classes such as " +
					"parameter and return types found in main class").required(false).build();

	static final Option EXCLUDE_CLASS_OPTION =
			Option.builder("e").longOpt("exclude-class")
					.desc("list of classes (separated by commas) " +
							"to exclude classes from document generation")
					.required(false).hasArg().argName("list").valueSeparator(' ').build();

	static final Options LUA_OPTIONS = new Options();
	static final Options JAVA_OPTIONS = new Options();

	static
	{
		LUA_OPTIONS.addOption(clone(INPUT_OPTION))
				.addOption(clone(OUTPUT_OPTION));

		OptionGroup javaOptGroup = createRequiredOptionGroup(
				clone(INPUT_OPTION), clone(API_OPTION)
		);
		JAVA_OPTIONS.addOptionGroup(javaOptGroup)
				.addOption(clone(OUTPUT_OPTION))
				.addOption(INCLUDE_REFS_OPTION)
				.addOption(EXCLUDE_CLASS_OPTION);
	}

	private static Option clone(Option option) {
		return (Option) option.clone();
	}

	private static OptionGroup createRequiredOptionGroup(Option... options) {

		OptionGroup optionGroup = new OptionGroup();
		for (Option option : options) {
			optionGroup.addOption(option);
		}
		optionGroup.setRequired(true);
		return optionGroup;
	}
}
