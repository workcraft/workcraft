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

package org.workcraft.plugins.xmas;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.xmas.components.VisualForkComponent;
import org.workcraft.plugins.xmas.components.VisualFunctionComponent;
import org.workcraft.plugins.xmas.components.VisualJoinComponent;
import org.workcraft.plugins.xmas.components.VisualMergeComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualSwitchComponent;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.util.Hierarchy;


@DisplayName("xMAS Circuit")
@ShortName("xMAS")
@CustomTools ( XmasToolsProvider.class )
public class VisualXmas extends AbstractVisualModel {

	private Xmas circuit;

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (!(first instanceof VisualXmasContact) || !(second instanceof VisualXmasContact)) {
			throw new InvalidConnectionException ("Connection is only allowed between ports");
		} else {
			if (((VisualXmasContact)first).getIOType() != IOType.OUTPUT) {
				throw new InvalidConnectionException ("Connection is only allowed from output port");
			}
			if (((VisualXmasContact)second).getIOType() != IOType.INPUT) {
				throw new InvalidConnectionException ("Connection is only allowed to input port");
			}
			for (Connection c: this.getConnections(first)) {
				if (c.getFirst() == first) {
					throw new InvalidConnectionException ("Only one connection is allowed from port");
				}
			}
			for (Connection c: this.getConnections(second)) {
				if (c.getSecond() == second) {
					throw new InvalidConnectionException ("Only one connection is allowed to port");
				}
			}
		}
	}

	public VisualXmas(Xmas model, VisualGroup root)
	{
		super(model, root);
		circuit=model;
	}

	public VisualXmas(Xmas model) throws VisualModelInstantiationException {
		super(model);
		circuit=model;
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	@Override
	public void connect(Node first, Node second)
			throws InvalidConnectionException {
		validateConnection(first, second);

		if (first instanceof VisualComponent && second instanceof VisualComponent) {
			VisualComponent c1 = (VisualComponent) first;
			VisualComponent c2 = (VisualComponent) second;
			MathConnection con = (MathConnection) circuit.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
			VisualXmasConnection connection = new VisualXmasConnection(con, c1, c2);
			Node parent = Hierarchy.getCommonParent(c1, c2);
			VisualGroup nearestAncestor = Hierarchy.getNearestAncestor (parent, VisualGroup.class);
			nearestAncestor.add(connection);
		}
	}

	public Collection<Node> getNodes() {
        ArrayList<Node> result =  new ArrayList<Node>();
        for (Node node : Hierarchy.getDescendantsOfType(getRoot(), Node.class)){
            if (node instanceof VisualSourceComponent)
                result.add(node);
            if (node instanceof VisualFunctionComponent)
                result.add(node);
            if (node instanceof VisualQueueComponent)
                result.add(node);
            if (node instanceof VisualForkComponent)
                result.add(node);
            if (node instanceof VisualJoinComponent)
                result.add(node);
            if (node instanceof VisualSwitchComponent)
                result.add(node);
            if (node instanceof VisualMergeComponent)
                result.add(node);
            if (node instanceof VisualSinkComponent)
                result.add(node);
        }
        return result;
	}
}
