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

import org.apache.commons.collections4.list.SetUniqueList;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.cocolabs.pz.zdoc.doc.ZomboidAPIDoc;
import io.cocolabs.pz.zdoc.element.java.JavaClass;
import io.cocolabs.pz.zdoc.element.java.JavaField;
import io.cocolabs.pz.zdoc.element.mod.AccessModifierKey;
import io.cocolabs.pz.zdoc.element.mod.MemberModifier;
import io.cocolabs.pz.zdoc.element.mod.ModifierKey;
import io.cocolabs.pz.zdoc.logger.Logger;

public class FieldDetail extends Detail<JavaField> {

	public FieldDetail(ZomboidAPIDoc document) throws DetailParsingException {
		super("field.detail", document);
	}

	@Override
	protected List<JavaField> parse() {

		Elements detail = getDetail();
		List<JavaField> result = new ArrayList<>();

		for (Element blockList : detail)
		{
			Element header = blockList.getElementsByTag("h4").first();
			String name = header != null ? header.text() : "unknown";

			Signature signature;
			try {
				Element eSignature = blockList.getElementsByTag("pre").first();
				if (eSignature == null) {
					throw new DetailParsingException("Unable to find field signature for field: " + name);
				}
				signature = new Signature(qualifyZomboidClassElements(eSignature), blockList);
			}
			catch (DetailParsingException e)
			{
				Logger.error(e.getMessage());
				continue;
			}
			JavaClass type = TypeSignatureParser.parse(signature.type);
			if (type != null) {
				result.add(new JavaField(type, signature.name, signature.modifier, signature.comment));
			}
			else Logger.detail(String.format("Excluding field (%s) from detail, " +
					"class %s does not exist", signature.toString(), signature.type));
		}
		return result;
	}

	public @Nullable JavaField getEntry(String name) {
		return getEntries().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}

	static String parseFieldComments(Element element) {

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
		String result = commentBuilder.toString();
		if (!result.isEmpty()) {
			Logger.debug("Parsed detail comment: \"" + result + "\"");
		}
		return result;
	}

	@Override
	public Set<JavaField> getEntries(String name) {
		return Sets.newHashSet(getEntry(name));
	}

	static class Signature extends DetailSignature {

		final MemberModifier modifier;
		final String type, name, comment;

		Signature(String signatureText, String detailComment) throws SignatureParsingException {
			super(signatureText);
			Logger.debug("Parsing field signature: " + signature);

			List<String> elements = Splitter.onPattern("\\s+").splitToList(signature);
			if (elements.size() < 2) {
				throw new SignatureParsingException(signature, "missing one or more elements");
			}
			int index = 0;
			/*
			 * parse signature access modifier (optional)
			 */
			AccessModifierKey access = AccessModifierKey.get(elements.get(0));
			if (access != AccessModifierKey.DEFAULT) {
				index = 1;
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
			String[] data = new String[]{ "type", "name" };
			for (int i = 0; i < data.length; i++, index++)
			{
				if (index < elements.size()) {
					data[i] = elements.get(index);
				}
				else throw new SignatureParsingException(signature, "missing element " + data[0]);
			}
			this.type = data[0];
			this.name = data[1];
			/*
			 * parse signature comment (optional)
			 */
			String sComment = "";
			if (index < elements.size())
			{
				StringBuilder sb = new StringBuilder();
				for (; index < elements.size(); index++) {
					sb.append(elements.get(index)).append(" ");
				}
				sb.deleteCharAt(sb.length() - 1);
				sComment = sb.toString();
			}
			if (detailComment != null && !detailComment.isEmpty()) {
				sComment += !sComment.isEmpty() ? '\n' + detailComment : detailComment;
			}
			this.comment = sComment;
		}

		Signature(String signatureText) throws SignatureParsingException {
			this(signatureText, "");
		}

		private Signature(Element element, Element parentElement) throws SignatureParsingException {
			this(element.text(), parseFieldComments(parentElement));
		}
	}
}
