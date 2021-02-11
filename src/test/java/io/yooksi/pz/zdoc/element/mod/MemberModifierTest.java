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
package io.yooksi.pz.zdoc.element.mod;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class MemberModifierTest {

	private static final AccessModifierKey PUBLIC = AccessModifierKey.PUBLIC;
	private static final ModifierKey UNDECLARED = ModifierKey.UNDECLARED;

	private static final TestMember STATIC_FINAL_MEMBER = new TestMember(
			AccessModifierKey.PUBLIC, ModifierKey.STATIC, ModifierKey.FINAL
	);
	private static final Object staticFinalField = null;
	private static Object staticField;
	public Object publicField;
	protected Object protectedField;
	Object defaultField;
	private Object privateField;

	@TestOnly
	private static Field getDeclaredField(String name) {

		return Arrays.stream(MemberModifierTest.class.getDeclaredFields())
				.filter(f -> f.getName().equals(name)).findFirst()
				.orElseThrow(RuntimeException::new);
	}

	@TestOnly
	private static Method getDeclaredMethod(String name) {

		return Arrays.stream(TestMember.class.getDeclaredMethods())
				.filter(f -> f.getName().equals(name)).findFirst()
				.orElseThrow(RuntimeException::new);
	}

	@Test
	@SuppressWarnings("ConstantConditions")
	void shouldThrowExceptionWhenModifyingModifierList() {
		Assertions.assertThrows(UnsupportedOperationException.class, () ->
				STATIC_FINAL_MEMBER.getModifiers().add(UNDECLARED));
	}

	@Test
	void shouldCreateMemberModifierWithValidReadableForm() {
		Assertions.assertEquals("public static final", STATIC_FINAL_MEMBER.toString());
	}

	@Test
	void shouldNotCreateMemberModifierWithKeysContainingUndeclaredKey() {

		TestMember undeclaredMember = new TestMember(PUBLIC, UNDECLARED);
		Assertions.assertTrue(undeclaredMember.isModifierUndeclared());

		TestMember finalMember = new TestMember(PUBLIC, UNDECLARED, ModifierKey.FINAL);
		Assertions.assertFalse(finalMember.isModifierUndeclared());
	}

	@Test
	void shouldCreateMemberModifierAsUndeclaredWhenNoModifiersPresent() {

		TestMember undeclaredMember = new TestMember(PUBLIC);
		Assertions.assertEquals(1, undeclaredMember.getModifiers().size());
		Assertions.assertTrue(undeclaredMember.isModifierUndeclared());
	}

	@Test
	void shouldCreateMemberModifierWithSpecifiedAccess() {
		Assertions.assertTrue(new TestMember(PUBLIC).hasAccess(PUBLIC));
	}

	@Test
	void shouldGetCorrectMemberModifierKeysByName() {

		for (AccessModifierKey key : AccessModifierKey.values()) {
			Assertions.assertEquals(key, AccessModifierKey.get(key.name));
		}
		ModifierKey[] keys = Arrays.stream(ModifierKey.values())
				.filter(k -> k != ModifierKey.UNDECLARED).toArray(ModifierKey[]::new);

		for (ModifierKey key : keys)
		{
			SetUniqueList<ModifierKey> foundKeys = ModifierKey.get(key.name);
			Assertions.assertEquals(1, foundKeys.size());
			Assertions.assertEquals(key, foundKeys.get(0));
		}
		List<String> keyNames = new ArrayList<>(keys.length);
		Arrays.stream(keys).forEach(k -> keyNames.add(k.name));

		String[] keyArray = keyNames.toArray(new String[]{});
		SetUniqueList<ModifierKey> modifierKeys = ModifierKey.get(keyArray);
		Assertions.assertEquals(keyArray.length, modifierKeys.size());

		for (int i = 0; i < modifierKeys.size(); i++) {
			Assertions.assertEquals(keys[i], modifierKeys.get(i));
		}
	}

	@Test
	void shouldGetCorrectMemberModifierKeysByModifiersValue() {

		for (AccessModifierKey key : AccessModifierKey.values()) {
			Assertions.assertEquals(key, AccessModifierKey.get(key.value));
		}
		for (ModifierKey key : ModifierKey.values())
		{
			SetUniqueList<ModifierKey> foundKeys = ModifierKey.get(key.value);
			Assertions.assertEquals(1, foundKeys.size());
			Assertions.assertEquals(key, foundKeys.get(0));
		}
	}

	@Test
	void shouldCorrectlyReadJavaFieldModifier() {

		TestMember staticMod = new TestMember(getDeclaredField("staticField"));
		Assertions.assertEquals(1, staticMod.getModifiers().size());
		Assertions.assertEquals("static", staticMod.getModifiers().get(0).toString());

		TestMember staticFinalMod = new TestMember(getDeclaredField("staticFinalField"));
		Assertions.assertEquals(2, staticFinalMod.getModifiers().size());
		Assertions.assertEquals("private static final", staticFinalMod.toString());
	}

	@Test
	void shouldCorrectlyReadJavaFieldAccessModifier() {

		String[] fieldData = new String[]{
				"public", "publicField",
				"protected", "protectedField",
				"private", "privateField",
				"", "defaultField"
		};
		for (int i = 0; i < fieldData.length; i += 2)
		{
			TestMember modifier = new TestMember(getDeclaredField(fieldData[i + 1]));
			Assertions.assertEquals(fieldData[i], modifier.getAccess().toString());
		}
	}

	@Test
	void shouldCorrectlyReadJavaMethodAccessModifier() {

		String[] methodData = new String[]{
				"public", "publicMethod",
				"protected", "protectedMethod",
				"private", "privateMethod",
				"", "defaultMethod"
		};
		for (int i = 0; i < methodData.length; i += 2)
		{
			TestMember modifier = new TestMember(getDeclaredMethod(methodData[i + 1]));
			Assertions.assertEquals(methodData[i], modifier.getAccess().toString());
		}
	}

	@Test
	void whenComparingMemberModifierWithEqualsShouldCompareInternalDetails() {

		// compare modifiers with same access
		Assertions.assertEquals(
				new MemberModifier(AccessModifierKey.PUBLIC),
				new MemberModifier(AccessModifierKey.PUBLIC)
		);
		Assertions.assertNotEquals(
				new MemberModifier(AccessModifierKey.PUBLIC),
				new MemberModifier(AccessModifierKey.PRIVATE)
		);
		// compare modifiers with different number of keys
		ModifierKey[] keys = new ModifierKey[]{
				ModifierKey.FINAL, ModifierKey.STATIC
		};
		MemberModifier modifier = new MemberModifier(AccessModifierKey.DEFAULT, keys);
		Assertions.assertEquals(
				new MemberModifier(AccessModifierKey.DEFAULT, keys), modifier
		);
		Assertions.assertNotEquals(
				MemberModifier.UNDECLARED, modifier
		);
		// compare modifiers with different key ordering
		Assertions.assertNotEquals(
				modifier, new MemberModifier(AccessModifierKey.DEFAULT,
						ModifierKey.STATIC, ModifierKey.FINAL)
		);
	}

	@SuppressWarnings({ "WeakerAccess", "RedundantSuppression" })
	private static class TestMember extends MemberModifier {

		private TestMember(Member member) {
			super(member.getModifiers());
		}

		private TestMember(AccessModifierKey accessKey, ModifierKey... modifierKeys) {
			super(accessKey, modifierKeys);
		}

		@TestOnly
		public void publicMethod() {
		}

		@TestOnly
		protected void protectedMethod() {
		}

		@TestOnly
		private void privateMethod() {
		}

		@TestOnly
		void defaultMethod() {
		}
	}
}
