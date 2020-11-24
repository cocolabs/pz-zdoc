package io.yooksi.pz.luadoc.lang;

public interface ElementParser<T extends ParseResult> {
	T parse(String text);
}
