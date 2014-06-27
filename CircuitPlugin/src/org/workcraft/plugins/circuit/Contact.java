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

package org.workcraft.plugins.circuit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

@DisplayName("Contact")
@VisualClass(org.workcraft.plugins.circuit.VisualContact.class)

public class Contact extends MathNode implements BooleanVariable {

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

	private String name = "";
	private boolean initOne = false;

	public boolean getInitOne() {
		return initOne;
	}

	public void setInitOne(boolean initOne) {
		this.initOne = initOne;
		sendNotification(new PropertyChangedEvent(this, "initOne"));
	}

	public Contact() {
	}

	public Contact(IOType iot) {
		super();
		setIOType(iot);
	}

	static public String getNewName(Node paren, String prefix, Node node, boolean allowShort) {
		// iterate through all contacts, check that the name doesn't exist
		int index=0;
		boolean found = false;
		if (allowShort) {
			for (Node n : paren.getChildren()) {
				if (n instanceof Contact && n != node) {
					if (((Contact)n).getName().equals(prefix)) {
						found = true;
						break;
					}
				}
			}
			if (!found) return prefix;
		}
		do {
			found = false;
			index++;
			for (Node n : paren.getChildren()) {
				if (n instanceof Contact && n != node) {
					if (((Contact)n).getName().equals(prefix + index)) {
						found = true;
						break;
					}
				}
			}
		} while (found);
		return (prefix + index);
	}

	public void checkName(Node parent) {
		if (parent == null) return;
		String prefix = getName();
		if (prefix == null || prefix == "") {
			if (getIOType()==IOType.INPUT) {
				prefix="input";
			} else {
				prefix="output";
			}
			setName(getNewName(parent, prefix, this, false));
		}
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
		checkName(parent);
	}

	public void setIOType(IOType t) {
		this.ioType = t;
		if (getName().startsWith("input") && getIOType()==IOType.OUTPUT) {
			setName(getNewName(getParent(), "output", this, false));
		} else if (getName().startsWith("output") && getIOType()==IOType.INPUT) {
			setName(getNewName(getParent(), "input", this, false));
		}
		sendNotification(new PropertyChangedEvent(this, "ioType"));
	}

	public IOType getIOType() {
		return ioType;
	}

	public void setName(String name) {
		this.name = name;
		sendNotification(new PropertyChangedEvent(this, "name"));
	}

	public String getName() {
		return name;
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String getLabel() {
		return getName();
	}

}
