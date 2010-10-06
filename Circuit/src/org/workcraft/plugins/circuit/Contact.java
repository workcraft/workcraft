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

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

@DisplayName("Contact")
@VisualClass("org.workcraft.plugins.circuit.VisualContact")

public class Contact extends MathNode implements BooleanVariable {

	public enum IOType { INPUT, OUTPUT};
	private IOType ioType = IOType.OUTPUT;

	private String name = "";


	//private boolean invertSignal = false;

	public Contact() {
	}


	public Contact(IOType iot) {
		super();

		setIOType(iot);
	}


	public String getNewName(Node n, String start) {
		// iterate through all contacts, check that the name doesn't exist
		int num=0;
		boolean found = true;

		while (found) {
			num++;
			found=false;

			for (Node vn : n.getChildren()) {
				if (vn instanceof Contact&& vn!=this) {
					if (((Contact)vn).getName().equals(start+num)) {
						found=true;
						break;
					}
				}
			}
		}
		return start+num;
	}

	public void checkName(Node parent) {
		if (parent==null) return;
		String start=getName();
		if (start==null||start=="") {
			if (getIOType()==IOType.INPUT) {
				start="input";
			} else {
				start="output";
			}
			setName(getNewName(parent, start));
		}
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
		checkName(parent);
	}


	public void setIOType(IOType t) {
		this.ioType = t;
		if (getName().startsWith("input")&&getIOType()==IOType.OUTPUT) {
			setName(getNewName(getParent(), "output"));
		} else if (getName().startsWith("output")&&getIOType()==IOType.INPUT) {
			setName(getNewName(getParent(), "input"));
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
