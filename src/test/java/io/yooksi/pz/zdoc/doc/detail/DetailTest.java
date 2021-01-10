/*
 * ZomboidDoc - Project Zomboid API parser and lua compiler.
 * Copyright (C) 2020 Matthew Cain
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.yooksi.pz.zdoc.UnitTest;
import io.yooksi.pz.zdoc.doc.DocTest;
import io.yooksi.pz.zdoc.element.IMember;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;

public class DetailTest extends DetailTestFixture<DetailTest.TestDetail> implements UnitTest {

	DetailTest() throws DetailParsingException {
		super(new TestDetail());
	}

	@Test
	void shouldGetValidParsedDetailObject() {

		String[] entryNames = new String[]{
				"innerClass", "getData"
		};
		Elements detail = this.detail.getDetail();
		Assertions.assertEquals(entryNames.length, detail.size());

		for (int i = 0; i < detail.size(); i++)
		{
			Element blockList = detail.get(i);
			Element header = blockList.getElementsByTag("h4").first();
			Assertions.assertEquals(entryNames[i], header.text());
		}
	}

	@Test
	void shouldCorrectlyQualifyZomboidClassElements() throws DetailParsingException {

		LinkedHashMap<String, String> qualifiedNames = new LinkedHashMap<>();
		qualifiedNames.put(
				"public Test.InnerClass innerClass",
				"public zombie.Test.InnerClass innerClass"
		);
		qualifiedNames.put(
				"public float getData(TestData data)",
				"public float getData(zombie.TestData data)"
		);
		Elements detail = this.detail.getDetail();
		Assertions.assertEquals(qualifiedNames.size(), detail.size());

		int i = -1;
		for (Map.Entry<String, String> entry : qualifiedNames.entrySet())
		{
			Element blockList = detail.get(i += 1);
			Element pre = blockList.getElementsByTag("pre").first();
			Assertions.assertEquals(entry.getKey(), DetailSignature.normalizeElementText(pre));

			Element signature = this.detail.qualifyZomboidClassElements(pre);
			Assertions.assertEquals(entry.getValue(), DetailSignature.normalizeElementText(signature));
		}
	}

	@Test
	void shouldThrowExceptionWhenModifyingDetailMembers() {
		Assertions.assertThrows(UnsupportedOperationException.class,
				() -> new TestDetail().getEntries().add(new TestMember()));
	}

	private static class TestMember implements IMember {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public MemberModifier getModifier() {
			return null;
		}

		@Override
		public String getComment() {
			return null;
		}
	}

	static class TestDetail extends Detail<TestMember> {

		public TestDetail() throws DetailParsingException {
			super("test.detail", DocTest.document);
		}

		@Override
		protected List<TestMember> parse() {
			return List.of(new TestMember());
		}
	}
}
