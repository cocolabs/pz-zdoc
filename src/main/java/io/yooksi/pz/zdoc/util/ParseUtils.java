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
package io.yooksi.pz.zdoc.util;

import java.util.regex.MatchResult;

import org.jetbrains.annotations.NotNull;

public class ParseUtils {

	/**
	 * @param match {@code MatchResult} that holds the captured group.
	 * @param group the index of a capturing group in this matcher's pattern
	 * @return input subsequence captured by {@code Matcher} for the given group
	 * 		or an empty string if the group failed to match part of the input.
	 *
	 * @throws IllegalStateException if the given {@code Matcher} has
	 * 		not attempted a match, or if the previous match operation failed
	 * @throws IndexOutOfBoundsException if no capturing group with the
	 * 		given index could be found by {@code Matcher}.
	 */
	public static @NotNull String getOptionalMatchedGroup(MatchResult match, int group) {

		String result = match.group(group);
		return result != null ? result : "";
	}

	public static String flushStringBuilder(StringBuilder sb) {

		String result = sb.toString();
		sb.setLength(0);
		return result;
	}
}
