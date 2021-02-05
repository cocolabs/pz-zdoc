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
package io.yooksi.pz.zdoc.doc.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.yooksi.pz.zdoc.doc.ZomboidAPIDoc;
import io.yooksi.pz.zdoc.element.java.JavaClass;
import io.yooksi.pz.zdoc.element.java.JavaMethod;
import io.yooksi.pz.zdoc.element.java.JavaParameter;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;
import io.yooksi.pz.zdoc.logger.Logger;
import io.yooksi.pz.zdoc.util.ParseUtils;

public class MethodDetail extends Detail<JavaMethod> {

	public MethodDetail(ZomboidAPIDoc document) throws DetailParsingException {
		super("method.detail", document);
	}

	@Override
	protected List<JavaMethod> parse() throws DetailParsingException {

		Elements detail = getDetail();
		List<JavaMethod> result = new ArrayList<>();

		for (Element blockList : detail)
		{
			Element listHeader = blockList.getElementsByTag("h4").first();
			String listName = listHeader != null ? listHeader.text() : "unknown";

			Element eSignature = blockList.getElementsByTag("pre").first();
			if (eSignature == null)
			{
				Logger.error("Unable to find method signature for method: " + listName);
				continue;
			}
			Signature signature = new Signature(qualifyZomboidClassElements(eSignature));
			JavaClass type = TypeSignatureParser.parse(signature.returnType);
			if (type == null)
			{
				String msg = "Excluding method (%s) from detail, class %s does not exist";
				Logger.detail(String.format(msg, signature.toString(), signature.returnType));
				continue;
			}
			List<JavaParameter> params = new ArrayList<>();
			boolean isVarArgs = false;
			if (!signature.params.isEmpty())
			{
				try {
					MethodSignatureParser parser = new MethodSignatureParser(signature.params);
					params = parser.parse();
					isVarArgs = parser.isVarArg();
				}
				catch (SignatureParsingException e)
				{
					String msg = "Excluding method (%s) from detail - %s.";
					Logger.printf(e.getLogLevel(), String.format(msg, signature.toString(), e.getMessage()));
					continue;
				}
			}
			result.add(new JavaMethod(
					signature.name, type, params, signature.modifier, isVarArgs, signature.comment)
			);
		}
		return result;
	}

	@Override
	public Set<JavaMethod> getEntries(String name) {
		return getEntries().stream().filter(e -> e.getName().equals(name)).collect(Collectors.toSet());
	}

	static class Signature extends DetailSignature {

		final MemberModifier modifier;
		final String returnType, name, params, comment;

		Signature(String signatureText) throws SignatureParsingException {
			super(signatureText);
			Logger.debug("Parsing method signature: " + signature);

			String[] elements = signature.split("\\s+");
			if (elements.length < 2) {
				throw new SignatureParsingException(signature, "missing one or more elements");
			}
			int index = 0;
			/*
			 * parse signature annotation (optional)
			 */
			String element = elements[index];
			String annotation = element.charAt(0) == '@' ? element.substring(index++) : "";
			/*
			 * parse signature access modifier (optional)
			 */
			AccessModifierKey access = AccessModifierKey.get(elements[index]);
			if (access != AccessModifierKey.DEFAULT) {
				index += 1;
			}
			/*
			 * parse signature non-access modifier (optional)
			 */
			SetUniqueList<ModifierKey> modifierKeys = SetUniqueList.setUniqueList(new ArrayList<>());
			for (; index < elements.length; index++)
			{
				Collection<ModifierKey> foundKeys = ModifierKey.get(elements[index]);
				if (!foundKeys.contains(ModifierKey.UNDECLARED)) {
					modifierKeys.addAll(foundKeys);
				}
				else break;
			}
			if (modifierKeys.isEmpty()) {
				modifierKeys.add(ModifierKey.UNDECLARED);
			}
			this.modifier = new MemberModifier(access, modifierKeys);
			/*
			 * parse signature type and name
			 */
			String type;
			if (index < elements.length) {
				type = elements[index];
			}
			else throw new SignatureParsingException(signature, "missing element type");
			this.returnType = type;

			String name = null;
			StringBuilder sb = new StringBuilder();
			for (char c : elements[index += 1].toCharArray())
			{
				if (c == '(') {
					name = ParseUtils.flushStringBuilder(sb);
				}
				else sb.append(c);
			}
			if (name == null) {
				throw new SignatureParsingException(signature, "missing element name");
			}
			this.name = name;
			/*
			 * parse signature parameters
			 */
			String paramsSegment = sb.toString();
			if (paramsSegment.charAt(0) != ')')
			{
				String params = null;
				for (index += 1; index < elements.length && params == null; index++)
				{
					sb.append(" ");
					for (char c : elements[index].toCharArray())
					{
						if (c == ')') {
							params = ParseUtils.flushStringBuilder(sb);
						}
						else sb.append(c);
					}
				}
				if (params == null)
				{
					// most probably dealing with vararg parameter here
					if (paramsSegment.endsWith(")")) {
						params = ParseUtils.flushStringBuilder(sb.deleteCharAt(sb.length() - 1));
					}
					else throw new SignatureParsingException(signature, "malformed element params");
				}
				this.params = params;
			}
			else {
				index += 1;
				sb.deleteCharAt(0);
				this.params = "";
			}
			/*
			 * parse signature comment (optional)
			 */
			for (; index < elements.length; index++) {
				sb.append(" ").append(elements[index]);
			}
			String tComment = sb.toString().trim();
			if (!annotation.isEmpty())
			{
				String commentSuffix = "This method is annotated as " + annotation;
				tComment = tComment.isEmpty() ? commentSuffix : tComment + '\n' + commentSuffix;
			}
			this.comment = tComment;
		}

		private Signature(Element element) throws SignatureParsingException {
			this(element.text());
		}
	}
}
