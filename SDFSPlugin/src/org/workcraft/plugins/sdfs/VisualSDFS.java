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

package org.workcraft.plugins.sdfs;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;

@DisplayName("Static Data Flow Structures")
@CustomTools ( SDFSToolsProvider.class )
@DefaultCreateButtons ( { Register.class, Logic.class } )
public class VisualSDFS extends AbstractVisualModel {

	public VisualSDFS(SDFS model)
	throws VisualModelInstantiationException {
		super(model);
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first == null || second == null) {
			throw new InvalidConnectionException ("Connections not valid");
		}
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);
		VisualComponent c1 = (VisualComponent) first;
		VisualComponent c2 = (VisualComponent) second;
		MathNode ref1 = c1.getReferencedComponent();
		MathNode ref2 = c2.getReferencedComponent();
		MathConnection con = ((SDFS)getMathModel()).connect(ref1, ref2);
		VisualConnection ret = new VisualConnection(con, c1, c2);
		Hierarchy.getNearestContainer(c1, c2).add(ret);
	}
}