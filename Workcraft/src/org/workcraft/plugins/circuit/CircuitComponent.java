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

import java.util.HashSet;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.Contact.IOType;

@DisplayName("Component")
@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

public class CircuitComponent extends MathNode {

	private HashSet<Contact> inputs = new HashSet<Contact>();
	private HashSet<Contact> outputs = new HashSet<Contact>();


	public Contact addInput() {
		Contact c = new Contact(IOType.INPUT);

		inputs.add(c);
		c.setParent(this);
		return c;
	}

	public Contact addOutput() {
		Contact c = new Contact(IOType.OUTPUT);
		outputs.add(c);
		c.setParent(this);
		return c;
	}

	public void removeContact(Contact c) {
		if (inputs.contains(c)) {
			inputs.remove(c);
		}
		if (outputs.contains(c)) {
			outputs.remove(c);
		}
	}

}
