/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.parsers.breeze;

import java.util.List;

import org.workcraft.parsers.breeze.dom.RawBreezePartReference;

public class BreezePartReference {
	private final BreezeLibrary library;
	private final RawBreezePartReference ref;

	public BreezePartReference(BreezeLibrary library, RawBreezePartReference ref) {
		this.library = library;
		this.ref = ref;
	}

	public BreezeDefinition definition() {
		BreezeDefinition def = library.get(ref.name());
		if(def == null)
			throw new RuntimeException("Definition not found for component " + ref.name());
		return def;
	}

	public ParameterValueList parameters() {
		final List<String> parameters = ref.parameters();
		return new ParameterValueList()
		{
			@Override public String get(int index) {
				return parameters.get(index);
			}

			@Override public int size() {
				return parameters.size();
			}
		};
	}

	public List<List<Integer>> connections() {
		return ref.connections();
	}
}
