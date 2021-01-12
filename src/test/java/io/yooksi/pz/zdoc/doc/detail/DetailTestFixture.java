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

import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.commons.util.StringUtils;

import com.google.common.collect.Sets;

import io.yooksi.pz.zdoc.doc.DocTest;
import io.yooksi.pz.zdoc.element.mod.AccessModifierKey;
import io.yooksi.pz.zdoc.element.mod.MemberModifier;
import io.yooksi.pz.zdoc.element.mod.ModifierKey;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DetailTestFixture<T extends Detail<?>> extends DocTest {

	static final String[] PRIMITIVE_TYPES = new String[]{
			"boolean", "byte", "char", "short", "int",
			"long", "float", "double", "void"
	};
	private static final Set<Set<ModifierKey>> MODIFIER_KEY_COMBINATIONS =
			Sets.powerSet(Sets.newHashSet(ModifierKey.values()));

	final T detail;

	DetailTestFixture(@NotNull T detail) {
		this.detail = detail;
	}

	@TestOnly
	static <T extends SignatureSupplier<?>>
	void assertMatchInFieldSignature(String text, String expected, Class<T> supplier) {

		for (AccessModifierKey access : AccessModifierKey.values())
		{
			for (Set<ModifierKey> modifierKeys : MODIFIER_KEY_COMBINATIONS)
			{
				ModifierKey[] keyArray = modifierKeys.toArray(new ModifierKey[0]);
				MemberModifier modifier = new MemberModifier(access, keyArray);

				String sModifier = modifier.toString();
				String sSignature = (!StringUtils.isBlank(sModifier) ? sModifier + " " : "") + text;

				try {
					T signature = supplier.getDeclaredConstructor(String.class).newInstance(sSignature);
					Assertions.assertEquals(expected, signature.get());
				}
				catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	static abstract class SignatureSupplier<T extends DetailSignature> implements Supplier<String> {

		final T signature;

		SignatureSupplier(T signature) {
			this.signature = signature;
		}
	}
}
