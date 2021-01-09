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
package io.yooksi.pz.zdoc.lang;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents an object that has data parsing functionality.
 *
 * @param <T> parsing result object type.
 * @param <I> input type data.
 */
public abstract class DataParser<T, I> {

	/** Data to be parsed by the parser. */
	public final I data;

	protected DataParser(@NotNull I data) {
		this.data = data;
	}

	/**
	 * Run parsing operation on designated data.
	 *
	 * @return this parser instance.
	 */
	abstract public T parse();
}
