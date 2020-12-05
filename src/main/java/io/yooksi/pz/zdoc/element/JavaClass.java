package io.yooksi.pz.zdoc.element;

/**
 * This class represents a parsed non-java-native class reference.
 */
public class JavaClass<L> extends MemberClass {

	private final L location;

	public JavaClass(String name, L location) {
		super(name);
		this.location = location;
	}

	public L getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, location.toString());
	}
}
