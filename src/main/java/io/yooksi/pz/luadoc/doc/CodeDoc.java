package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.io.FileUtils;

import io.yooksi.pz.luadoc.element.MemberClass;
import io.yooksi.pz.luadoc.element.Method;
import io.yooksi.pz.luadoc.lang.ParseResult;

/**
 * This class represents a parsed code based document.
 *
 * @param <M> method type used by this document.
 */
@SuppressWarnings("unused")
public abstract class CodeDoc<M extends Method> implements ParseResult {

	private final String name;

	private final List<M> methods;

	/**
	 * Textual representation of this document ready for output.
	 */
	private final List<String> content;

	/**
	 * Textual representation of class fields or function parameters.
	 */
	private final Map<String, MemberClass> members;

	public CodeDoc(String name, List<String> content, Set<? extends MemberClass> members, List<M> methods) {

		this.name = name;
		this.content = content;
		this.methods = ListUtils.predicatedList(methods, PredicateUtils.notNullPredicate());

		this.members = new ConcurrentHashMap<>();
		members.forEach(m -> this.members.put(m.getName(), m));
	}

	public String getName() {
		return name;
	}

	public List<String> readContent() {
		return Collections.unmodifiableList(content);
	}

	public Map<String, ? extends MemberClass> getMembers() {
		return Collections.unmodifiableMap(members);
	}

	public List<M> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public void writeToFile(Path path) throws IOException {
		FileUtils.writeLines(path.toFile(), content, false);
	}
}
