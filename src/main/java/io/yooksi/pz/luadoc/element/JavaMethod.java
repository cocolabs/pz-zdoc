package io.yooksi.pz.luadoc.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.jetbrains.annotations.Nullable;

import io.yooksi.pz.luadoc.Main;
import io.yooksi.pz.luadoc.lang.DataParser;
import io.yooksi.pz.luadoc.lang.ParseRegex;

/**
 * This class represents a parsed Java method.
 */
public class JavaMethod extends Method {

	public JavaMethod(String modifier, String returnType, String name, Parameter[] params, String comment) {
		super(modifier, returnType, name, params, comment);
	}

	@Override
	public String toString() {

		String sParams = "";
		if (this.params.length > 0)
		{
			final StringBuilder sb = new StringBuilder();
			Arrays.stream(params).forEach(p -> sb.append(p.getUnqualified().toString()).append(", "));
			sParams = sb.substring(0, sb.length() - 2).trim();
		}
		return String.format("%s%s%s%s %s(%s)", !hasComment() ? "" : "\\\\" + getComment() +
				'\n', modifier, (modifier.length() > 0 ? ' ' : ""), returnType, name, sParams);
	}

	public static class Parser extends DataParser<JavaMethod, String> {

		private Parser(String data) {
			super(data);
		}

		public static Parser create(String data) {
			return new Parser(data);
		}

		@Override
		public @Nullable JavaMethod parse() {

			if (data == null) {
				throw new RuntimeException("Tried to parse null data");
			}
			Matcher matcher = ParseRegex.JAVA_METHOD_REGEX.matcher(data);
			if (matcher.find())
			{
				java.util.List<Parameter> paramList = new ArrayList<>();
				String paramsMatched = matcher.group(4);
				if (paramsMatched != null)
				{
					for (String param : paramsMatched.trim().split(",(?:\\s+)"))
					{
						String[] params = param.split("\\s+");
						paramList.add(new Parameter(params[0], params.length < 2 ? "" : params[1]));
					}
				}
				return new JavaMethod(ParseRegex.getMatchedGroup(matcher, 1),
						ParseRegex.getMatchedGroup(matcher, 2),
						ParseRegex.getMatchedGroup(matcher, 3),
						paramList.toArray(new Parameter[]{}),
						ParseRegex.getMatchedGroup(matcher, 5));
			}
			else {
				Main.LOGGER.warn("Unable to parse method data: " + data);
				return null;
			}
		}
	}
}
