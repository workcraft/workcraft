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

public class RawBreezePartReference {

	private final String name;
	private final List<String> parameters;
	private final List<List<Integer>> connections;

	public RawBreezePartReference(String name,
			List<String> parameters,
			List<List<Integer>> connections) {
				this.name = name;
				this.parameters = parameters;
				this.connections = connections;
	}

	public Object name() {
		return name;
	}

	public List<String> parameters() {
		return parameters;
	}

	public List<List<Integer>> connections() {
		return connections;
	}
}
