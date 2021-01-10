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
package io.yooksi.pz.zdoc.element.mod;

import org.apache.commons.collections4.list.SetUniqueList;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemberModifier {

	public static final MemberModifier UNDECLARED =
			new MemberModifier(AccessModifierKey.DEFAULT, ModifierKey.UNDECLARED);

	private final AccessModifierKey access;
	private final SetUniqueList<ModifierKey> modifiers;

	public MemberModifier(AccessModifierKey accessKey, List<ModifierKey> modifierKeys) {

		this.access = accessKey;
		this.modifiers = SetUniqueList.setUniqueList(modifierKeys);
		/*
		 * if no modifiers present mark as undeclared,
		 * otherwise make sure modifier is not marked as undeclared
		 */
		if (this.modifiers.isEmpty()) {
			this.modifiers.add(ModifierKey.UNDECLARED);
		}
		else if (this.modifiers.size() > 1) {
			this.modifiers.remove(ModifierKey.UNDECLARED);
		}
	}

	public MemberModifier(AccessModifierKey accessKey, ModifierKey...modifierKeys) {
		this(accessKey, Arrays.stream(modifierKeys).collect(Collectors.toList()));
	}

	public MemberModifier(int modifiers) {
		this(AccessModifierKey.get(modifiers), ModifierKey.get(modifiers));
	}

	public AccessModifierKey getAccess() {
		return access;
	}

	protected @Unmodifiable List<ModifierKey> getModifiers() {
		return Collections.unmodifiableList(modifiers);
	}

	public boolean hasAccess(AccessModifierKey key) {
		return access == key;
	}

	public boolean isModifierUndeclared() {
		return modifiers.contains(ModifierKey.UNDECLARED);
	}

	public boolean hasModifiers(ModifierKey...keys) {
		return Arrays.stream(keys).allMatch(modifiers::contains);
	}

	public boolean matchesModifiers(ModifierKey...keys) {
		return modifiers.equals(List.of(keys));
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(access.name).append(' ');
		modifiers.forEach(m -> sb.append(m.toString()).append(' '));
		return sb.toString().trim();
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MemberModifier modifier = (MemberModifier) obj;
		return access == modifier.access && modifiers.equals(modifier.modifiers);
	}

	@Override
	public int hashCode() {
		return 31 * access.hashCode() + modifiers.hashCode();
	}
}
