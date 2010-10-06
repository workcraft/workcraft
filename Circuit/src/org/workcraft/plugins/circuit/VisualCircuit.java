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

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.Connection;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;


@DisplayName("Visual Circuit")
@CustomTools ( CircuitToolsProvider.class )
@DefaultCreateButtons ( { Joint.class, CircuitComponent.class, Function.class } )
//@CustomToolButtons ( { SimulationTool.class } )

public class VisualCircuit extends AbstractVisualModel {

	private Circuit circuit;

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first instanceof VisualConnection || second instanceof VisualConnection) {
			throw new InvalidConnectionException ("Connecting with connections is not implemented yet");
		}
		if (first instanceof VisualComponent && second instanceof VisualComponent) {


			for (Connection c: this.getConnections(second)) {
				if (c.getSecond()==second)
					throw new InvalidConnectionException ("Only one connection is allowed as a driver");
			}

			if (second instanceof VisualContact) {
				Node toParent = ((VisualComponent)second).getParent();
				Contact.IOType toType = ((Contact)((VisualComponent)second).getReferencedComponent()).getIOType();

				if ((toParent instanceof VisualCircuitComponent) && toType == Contact.IOType.OUTPUT)
					throw new InvalidConnectionException ("Outputs of the components cannot be driven");

				if (!(toParent instanceof VisualCircuitComponent) && toType == Contact.IOType.INPUT)
					throw new InvalidConnectionException ("Inputs from the environment cannot be driven");
			}
		}
	}

/*
	private final class StateSupervisorExtension extends StateSupervisor {
		@Override
		public void handleEvent(StateEvent e) {
//			if(e instanceof PropertyChangedEvent)

		}
	}
*/

	public VisualCircuit(Circuit model, VisualGroup root)
	{
		super(model, root);
		circuit=model;
	}

	public VisualCircuit(Circuit model)
	throws VisualModelInstantiationException {
		super(model);
		circuit=model;
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}

		//new StateSupervisorExtension().attach(getRoot());
	}

	@Override
	public void connect(Node first, Node second)
			throws InvalidConnectionException {
		validateConnection(first, second);
		if (first instanceof VisualComponent && second instanceof VisualComponent) {

			VisualComponent c1 = (VisualComponent) first;
			VisualComponent c2 = (VisualComponent) second;
			MathConnection con = (MathConnection) circuit.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
			VisualConnection ret = new VisualConnection(con, c1, c2);
			Hierarchy.getNearestContainer(c1, c2).add(ret);
		}

	}

}
