package io.yooksi.pz.luadoc.lang;

import org.jetbrains.annotations.Nullable;

public abstract class DataParser<T extends ParseResult, I> {

	public @Nullable I data;

	public DataParser<T, I> input(I data) {

		this.data = data;
		return this;
	}

	abstract public T parse();
}
