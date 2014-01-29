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

package org.workcraft.plugins.xmas.components;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;

@DisplayName("Contact")
@VisualClass(org.workcraft.plugins.xmas.components.VisualXmasContact.class)
public class XmasContact extends MathNode {
	public enum IOType {
		INPUT("Input"),
		OUTPUT("Output");

		private final String name;

		private IOType(String name) {
			this.name = name;
		}

		static public Map<String, IOType> getChoice() {
			LinkedHashMap<String, IOType> choice = new LinkedHashMap<String, IOType>();
			for (IOType item : IOType.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	};

	private IOType ioType = IOType.OUTPUT;

	public XmasContact() {
	}

	public XmasContact(IOType ioType) {
		super();
		setIOType(ioType);
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
	}

	public IOType getIOType() {
		return ioType;
	}

	public void setIOType(IOType ioType) {
		this.ioType = ioType;
	}

}
