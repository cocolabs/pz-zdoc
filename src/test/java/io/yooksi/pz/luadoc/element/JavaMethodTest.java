package io.yooksi.pz.luadoc.element;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JavaMethodTest {

	@Test
	void shouldCorrectlyParseComplexJavaMethod() {

		JavaMethod.Parser parser = JavaMethod.Parser.create("static KahluaTable " +
				"transformIntoKahluaTable(java.util.HashMap<java.lang.Object," +
				"java.lang.Object> map, Object index)");

		JavaMethod method = parser.parse();
		Assertions.assertNotNull(method);

		Assertions.assertEquals("static", method.modifier);
		Assertions.assertEquals("KahluaTable", method.returnType);
		Assertions.assertEquals("transformIntoKahluaTable", method.name);

		Assertions.assertEquals(2, method.params.length);
		Assertions.assertEquals("map", method.params[0].getName(false));
		Assertions.assertEquals("HashMap<Object,Object>", method.params[0].getType(false));
	}
}
