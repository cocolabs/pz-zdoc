package io.yooksi.pz.luadoc.element;

/**
 * This class represents a parsed non-java-native class reference.
 */
public class JavaClass extends MemberClass {

	private final String path;

	public JavaClass(String name, String path) {
		super(name);
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, path);
	}
}
