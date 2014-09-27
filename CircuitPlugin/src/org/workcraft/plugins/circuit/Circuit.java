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

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.references.CircuitReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;


public class Circuit extends AbstractMathModel {

	public Circuit() {
		this(null, null);
	}

	public Circuit(MathGroup root) {
		this(root, null);
	}

	public Circuit(Container root, References refs) {
		super(root, new CircuitReferenceManager((NamespaceProvider)root, refs) {
			@Override
			public String getPrefix(Node node) {
				if (node instanceof CircuitComponent) return "g";
				if (node instanceof Joint) return "j";
				if (node instanceof Contact) {
					Contact contact = (Contact)node;
					if (contact.getParent() instanceof CircuitComponent) return "z";
					if (contact.getIOType() == IOType.INPUT) return "in";
					if (contact.getIOType() == IOType.OUTPUT) return "out";
				}
				return super.getPrefix(node);
			}
		});
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node != null)  {
			if (node instanceof Joint) {
				properties.removeByName("Name");
			}
		}
		return properties;
	}

}
