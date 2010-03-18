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

package org.workcraft.plugins.graph;

import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.PetriNet;

@DefaultCreateButtons ( { Vertex.class } )
public class VisualGraph extends AbstractVisualModel {

	public VisualGraph(Graph model) throws VisualModelInstantiationException {
		super(model);
	}

	public VisualGraph(Graph model, VisualGroup root) {
		super(model, root);

		if (root == null)
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}
	}


	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}

	@Override
	public void connect(Node first, Node second)
			throws InvalidConnectionException {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}