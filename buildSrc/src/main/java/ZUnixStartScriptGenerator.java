/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2021 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.gradle.api.NonNullApi;
import org.gradle.api.Transformer;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.plugins.StartScriptTemplateBindingFactory;
import org.gradle.jvm.application.scripts.JavaAppStartScriptGenerationDetails;
import org.gradle.jvm.application.scripts.ScriptGenerator;
import org.gradle.util.TextUtil;

@NonNullApi
public class ZUnixStartScriptGenerator implements ScriptGenerator {

	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final ClassLoader CL = ZUnixStartScriptGenerator.class.getClassLoader();

	private static final String[] CLASSPATH_ADDENDUM = new String[]{
			"$PZ_DIR_PATH",                // Project Zomboid game classes
			"$PZ_DIR_PATH/*"               // Project Zomboid libraries
	};
	private static final Pattern REGEX_TOKEN = Pattern.compile("%!(.*)!%");

	private final String scriptTemplate, lineSeparator;
	private final Transformer<Map<String, String>, JavaAppStartScriptGenerationDetails> bindingFactory;

	public ZUnixStartScriptGenerator() {
		this.lineSeparator = TextUtil.getUnixLineSeparator();
		this.scriptTemplate = readScriptResourceFile();
		this.bindingFactory = StartScriptTemplateBindingFactory.unix();
	}

	private static String readScriptResourceFile() {

		String filename = "unixScriptTemplate.sh";
		try (InputStream iStream = CL.getResourceAsStream(filename))
		{
			if (iStream == null) {
				throw new IllegalStateException("Unable to find file \"" + filename + "\"");
			}
			return IOUtils.toString(iStream, CHARSET);
		}
		catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public void generateScript(JavaAppStartScriptGenerationDetails details, Writer destination) {
		try {
			Map<String, String> binding = this.bindingFactory.transform(details);

			StringBuilder sb = new StringBuilder();
			sb.append(binding.get("classpath"));
			for (String entry : CLASSPATH_ADDENDUM) {
				sb.append(':').append(entry);
			}
			binding.put("classpath", sb.toString());
			destination.write(generateStartScriptContentFromTemplate(binding));
		}
		catch (IOException var5) {
			throw new UncheckedIOException(var5);
		}
	}

	private String generateStartScriptContentFromTemplate(final Map<String, String> binding) {

		Matcher matcher = REGEX_TOKEN.matcher(scriptTemplate);
		StringBuffer sb = new StringBuffer();
		/*
		 * all matched tokens will be replaced with binding params that match token name
		 * see: StartScriptTemplateBindingFactory#ScriptBindingParameter
		 */
		while (matcher.find())
		{
			String bindingParam = binding.get(matcher.group(1));
			matcher.appendReplacement(sb, Matcher.quoteReplacement(bindingParam));
		}
		matcher.appendTail(sb);
		return Objects.requireNonNull(TextUtil.convertLineSeparators(sb.toString(), lineSeparator));
	}
}
