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

import java.util.*;
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
			// parse method block and label comments
			StringBuilder commentBuilder = new StringBuilder();
			Elements commentBlocks = blockList.getElementsByClass("block");
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
			String returnTypeComment = "";
			Map<String, String> paramComments = new HashMap<>();
			Map<Element, Elements> descriptionList = new HashMap<>();

			Elements ddElements = new Elements();
			for (Element element : blockList.getAllElements())
			{
				// description list title
				if (element.tagName().equals("dt"))
				{
					ddElements = new Elements();
					descriptionList.put(element, ddElements);
				}
				// description list elements
				else if (element.tagName().equals("dd")) {
					ddElements.add(element);
				}
			}
			for (Map.Entry<Element, Elements> entry : descriptionList.entrySet())
			{
				Element listTitle = entry.getKey();
				Elements listEntries = entry.getValue();
				if (listEntries.isEmpty())
				{
					Logger.debug(String.format("Missing list elements for title '%s'", listTitle.text()));
					continue;
				}
				Element titleContainer = listTitle.getElementsByTag("span").first();
				// we're expecting to find list title in span container
				if (titleContainer == null)
				{
					Logger.error(String.format("Unexpected description list title '%s'", listTitle));
					continue;
				}
				String className = titleContainer.className();

				// include override method documentation
				//noinspection IfCanBeSwitch
				if (className.equals("overrideSpecifyLabel"))
				{
					if (commentBuilder.length() > 0) {
						commentBuilder.append('\n');
					}
					commentBuilder.append(listTitle.text());
					Element overrideLabelElement = listEntries.get(0);
					commentBuilder.append('\n').append(overrideLabelElement.text());
				}
				// include method return value documentation
				else if (className.equals("returnLabel"))
				{
					Element returnLabelElement = listEntries.get(0);
					returnTypeComment = returnLabelElement.text();
				}
				// include method parameter documentation
				else if (className.equals("paramLabel"))
				{
					for (Element listEntry : listEntries)
					{
						Element eParamName = listEntry.getElementsByTag("code").first();
						if (eParamName == null)
						{
							Logger.error(String.format("No paramLabel name found '%s'", listEntry));
							continue;
						}
						String sParamName = eParamName.text();
						// trim element text to get only parameter comment
						String paramText = listEntry.text().substring(sParamName.length() + 3);
						paramComments.put(sParamName, paramText);
					}
				}
			}
			String methodComment = commentBuilder.toString();
			if (!methodComment.isEmpty()) {
				Logger.debug("Parsed detail comment: \"" + result + "\"");
			}
			Signature signature = new Signature(qualifyZomboidClassElements(eSignature), methodComment);
			JavaClass type = TypeSignatureParser.parse(signature.returnType);
			if (type == null)
			{
				String msg = "Excluding method (%s) from detail, class %s does not exist";
				Logger.detail(String.format(msg, signature.toString(), signature.returnType));
				continue;
			}
			// rawParams is a list of parameter without comments
			List<JavaParameter> rawParams = new ArrayList<>();
			boolean isVarArgs = false;
			if (!signature.params.isEmpty())
			{
				try {
					MethodSignatureParser parser = new MethodSignatureParser(signature.params);
					rawParams = parser.parse();
					isVarArgs = parser.isVarArg();
				}
				catch (SignatureParsingException e)
				{
					String msg = "Excluding method (%s) from detail - %s.";
					Logger.printf(e.getLogLevel(), String.format(msg, signature.toString(), e.getMessage()));
					continue;
				}
			}
			// match parameters with their respected comments
			List<JavaParameter> params = new ArrayList<>();
			for (int i = 0; i < rawParams.size(); i++)
			{
				JavaParameter param = rawParams.get(i);
				String comment = paramComments.get(param.getName());
				if (comment != null) {
					params.add(i, new JavaParameter(param.getType(), param.getName(), comment));
				}
				// when no comment was found use raw parameter
				else params.add(i, param);
			}
			result.add(JavaMethod.Builder.create(signature.name)
					.withReturnType(type, returnTypeComment).withModifier(signature.modifier)
					.withParams(params).withVarArgs(isVarArgs).withComment(signature.comment).build());
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

		private Signature(Element element, String comment) throws SignatureParsingException {
			this(element.text(), comment);
		}
	}
}
