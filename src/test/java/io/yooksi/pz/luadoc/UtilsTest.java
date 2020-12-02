package io.yooksi.pz.luadoc;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	void shouldResolveUrlWithMultipleDirectories() throws MalformedURLException {

		URL url = new URL("https://worldwideweb.com");
		URL result = Utils.getURL(url, "one", "two", "three");

		String expected = "https://worldwideweb.com/one/two/three";
		Assertions.assertEquals(expected, result.toString());
	}
}
