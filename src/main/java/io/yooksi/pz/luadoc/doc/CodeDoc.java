package io.yooksi.pz.luadoc.doc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import io.yooksi.pz.luadoc.lang.ParseResult;
import io.yooksi.pz.luadoc.element.MemberClass;
import io.yooksi.pz.luadoc.element.Method;

public abstract class CodeDoc<M extends Method> implements ParseResult {

	protected final List<M> methods;
	private final List<String> content;
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
