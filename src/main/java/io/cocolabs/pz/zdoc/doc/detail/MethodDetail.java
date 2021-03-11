/*
 * ZomboidDoc - Lua library compiler for Project Zomboid
 * Copyright (C) 2020-2021 Matthew Cain
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
package io.cocolabs.pz.zdoc.doc.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;

import io.cocolabs.pz.zdoc.doc.ZomboidAPIDoc;
import io.cocolabs.pz.zdoc.element.java.JavaClass;
import io.cocolabs.pz.zdoc.element.java.JavaMethod;
import io.cocolabs.pz.zdoc.element.java.JavaParameter;
import io.cocolabs.pz.zdoc.element.mod.AccessModifierKey;
import io.cocolabs.pz.zdoc.element.mod.MemberModifier;
import io.cocolabs.pz.zdoc.element.mod.ModifierKey;
import io.cocolabs.pz.zdoc.logger.Logger;
import io.cocolabs.pz.zdoc.util.ParseUtils;

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
			Signature signature = new Signature(qualifyZomboidClassElements(eSignature), blockList);
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
			result.add(JavaMethod.Builder.create(signature.name)
					.withReturnType(type).withModifier(signature.modifier)
					.withParams(params).withVarArgs(isVarArgs)
					.withComment(signature.comment).build());
		}
		return result;
	}

	@Override
	public Set<JavaMethod> getEntries(String name) {
		return getEntries().stream().filter(e -> e.getName().equals(name)).collect(Collectors.toSet());
	}

	private static String parseMethodComments(Element element) {

		StringBuilder commentBuilder = new StringBuilder();
		Elements commentBlocks = element.getElementsByClass("block");
		if (!commentBlocks.isEmpty())
		{
			commentBuilder.append(commentBlocks.get(0).wholeText());
			/*
			 * normally there should only be one comment block per element
			 * but check for additional blocks just to be on the safe side
			 */
			for (int i = 1; i < commentBlocks.size(); i++) {
				commentBuilder.append('\n').append(commentBlocks.get(i).text());
			}
		}
		// include override method documentation
		boolean lastElementOverrideLabel = false;
		for (Element blockListElement : element.getAllElements())
		{
			String tagName = blockListElement.tagName();
			if (blockListElement.className().equals("overrideSpecifyLabel"))
			{
				if (commentBuilder.length() > 0) {
					commentBuilder.append('\n');
				}
				commentBuilder.append(blockListElement.text());
				lastElementOverrideLabel = true;
			}
			else if (lastElementOverrideLabel)
			{
				if (tagName.equals("dd")) {
					commentBuilder.append('\n').append(blockListElement.text());
				}
				lastElementOverrideLabel = false;
			}
		}
		String result = commentBuilder.toString();
		if (!result.isEmpty()) {
			Logger.debug("Parsed detail comment: \"" + result + "\"");
		}
		return result;
	}

	static class Signature extends DetailSignature {

		final MemberModifier modifier;
		final String returnType, name, params, comment;

		Signature(String signatureText, String detailComment) throws SignatureParsingException {
			super(signatureText);
			Logger.debug("Parsing method signature: " + signature);

			List<String> elements = Splitter.onPattern("\\s+").splitToList(signature);
			if (elements.size() < 2) {
				throw new SignatureParsingException(signature, "missing one or more elements");
			}
			int index = 0;
			/*
			 * parse signature annotation (optional)
			 */
			String element = elements.get(index);
			String annotation = element.charAt(0) == '@' ? element.substring(index++) : "";
			/*
			 * parse signature access modifier (optional)
			 */
			AccessModifierKey access = AccessModifierKey.get(elements.get(index));
			if (access != AccessModifierKey.DEFAULT) {
				index += 1;
			}
			/*
			 * parse signature non-access modifier (optional)
			 */
			SetUniqueList<ModifierKey> modifierKeys = SetUniqueList.setUniqueList(new ArrayList<>());
			for (; index < elements.size(); index++)
			{
				Collection<ModifierKey> foundKeys = ModifierKey.get(elements.get(index));
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
			if (index < elements.size()) {
				type = elements.get(index);
			}
			else throw new SignatureParsingException(signature, "missing element type");
			this.returnType = type;

			String name = null;
			StringBuilder sb = new StringBuilder();
			for (char c : elements.get(index += 1).toCharArray())
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
				for (index += 1; index < elements.size() && params == null; index++)
				{
					sb.append(" ");
					for (char c : elements.get(index).toCharArray())
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
			for (; index < elements.size(); index++) {
				sb.append(" ").append(elements.get(index));
			}
			String tComment = sb.toString().trim();
			if (!annotation.isEmpty())
			{
				String commentSuffix = "This method is annotated as " + annotation;
				tComment = tComment.isEmpty() ? commentSuffix : tComment + '\n' + commentSuffix;
			}
			if (detailComment != null && !detailComment.isEmpty()) {
				tComment += !tComment.isEmpty() ? '\n' + detailComment : detailComment;
			}
			this.comment = tComment;
		}

		Signature(String signatureText) throws SignatureParsingException {
			this(signatureText, "");
		}

		private Signature(Element element, Element parentElement) throws SignatureParsingException {
			this(element.text(), parseMethodComments(parentElement));
		}
	}
}
