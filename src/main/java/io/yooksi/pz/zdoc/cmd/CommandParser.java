/*
 * Copyright (C) 2020 Matthew Cain
 * ZomboidDoc - Lua library compiler for Project Zomboid
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

import java.util.Properties;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class CommandParser extends DefaultParser {

	@Override
	public CommandLine parse(Options options, String[] arguments) throws ParseException {
		return new CommandLine(super.parse(options, arguments));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException {
		return new CommandLine(super.parse(options, arguments, stopAtNonOption));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, Properties properties) throws ParseException {
		return new CommandLine(super.parse(options, arguments, properties));
	}

	@Override
	public CommandLine parse(Options options, String[] arguments, Properties properties,
							 boolean stopAtNonOption) throws ParseException {
		return new CommandLine(super.parse(options, arguments, properties, stopAtNonOption));
	}
}
