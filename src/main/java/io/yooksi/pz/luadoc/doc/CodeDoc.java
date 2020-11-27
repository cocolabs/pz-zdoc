package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

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

	protected final List<M> methods;

	/**
	 * Textual representation of this document ready for output.
	 */
	private final List<String> content;

	/**
	 * Textual representation of non-java native class references used by this document.
	 * These could be class fields or function parameters.
	 */
	private final List<MemberClass> members;

	public CodeDoc(List<String> content, List<MemberClass> members, List<M> methods) {

		this.content = content;
		this.methods = methods;
		this.members = members;
	}

	public List<String> readContent() {
		return Collections.unmodifiableList(content);
	}

	public List<MemberClass> getMembers() {
		return Collections.unmodifiableList(members);
	}

	public List<M> getMethods() {
		return Collections.unmodifiableList(methods);
	}

	public void writeToFile(Path path) throws IOException {
		FileUtils.writeLines(path.toFile(), content, false);
	}
}
