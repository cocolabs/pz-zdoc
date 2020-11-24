package io.yooksi.pzlua.tools.lang;

public interface ElementParser<T extends ParseResult> {

	T parse(String text);
}
