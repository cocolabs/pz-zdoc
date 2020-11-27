package io.yooksi.pz.luadoc.lang;

import org.jetbrains.annotations.Nullable;

/**
 * This class represents an object that has data parsing functionality.
 * In particular an object that produces {@link ParseResult} objects.
 *
 * @param <T> parsing result object type.
 * @param <I> input type data.
 */
public abstract class DataParser<T extends ParseResult, I> {

	/** Data to be parsed by the parser. */
	public @Nullable I data;

	/**
	 * Set data to be parsed by this parser.
	 *
	 * @param data data to be parsed.
	 * @return this parser instance.
	 */
	public DataParser<T, I> input(I data) {

		this.data = data;
		return this;
	}

	/**
	 * Run parsing operation on designated data.
	 *
	 * @return this parser instance.
	 */
	abstract public T parse();
}
