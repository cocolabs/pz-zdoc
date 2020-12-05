package io.yooksi.pz.zdoc.lang;

/**
 * This class represents an object that has data parsing functionality.
 * In particular an object that produces {@link ParseResult} objects.
 *
 * @param <T> parsing result object type.
 * @param <I> input type data.
 */
public abstract class DataParser<T extends ParseResult, I> {

	/** Data to be parsed by the parser. */
	public final I data;

	protected DataParser(I data) {
		this.data = data;
	}

	/**
	 * Run parsing operation on designated data.
	 *
	 * @return this parser instance.
	 */
	abstract public T parse();
}
