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
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.util.Hierarchy;

@DisplayName ("Digital Circuit")
@VisualClass ("org.workcraft.plugins.circuit.VisualCircuit")

public class Circuit extends AbstractMathModel {

	public Circuit() {
		super(null);
	}

	public void validate() throws ModelValidationException {
	}


	@Override
	public Node getNodeByReference(String reference) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public String getNodeReference(Node node) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}


	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {

		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);

		Hierarchy.getNearestContainer(first, second).add(con);

		return con;
	}

}
