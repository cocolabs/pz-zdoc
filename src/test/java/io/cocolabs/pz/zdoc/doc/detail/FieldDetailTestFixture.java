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

import io.cocolabs.pz.zdoc.doc.DocTest;

class FieldDetailTestFixture extends DetailTestFixture<FieldDetail> {

	FieldDetailTestFixture() throws DetailParsingException {
		super(new FieldDetail(DocTest.DOCUMENT));
	}

	abstract static class FieldSignatureSupplier extends SignatureSupplier<FieldDetail.Signature> {

		FieldSignatureSupplier(String signature) throws DetailParsingException {
			super(new FieldDetail.Signature(signature));
		}
	}

	static class TypeSupplier extends FieldSignatureSupplier {

		TypeSupplier(String signature) throws DetailParsingException {
			super(signature);
		}

		@Override
		public String get() {
			return signature.type;
		}
	}

	static class NameSupplier extends FieldSignatureSupplier {

		NameSupplier(String signature) throws DetailParsingException {
			super(signature);
		}

		@Override
		public String get() {
			return signature.name;
		}
	}

	static class CommentSupplier extends FieldSignatureSupplier {

		CommentSupplier(String signature) throws DetailParsingException {
			super(signature);
		}

		@Override
		public String get() {
			return signature.comment;
		}
	}
}
