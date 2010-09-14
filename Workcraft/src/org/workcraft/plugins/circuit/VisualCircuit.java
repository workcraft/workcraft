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
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;

@DisplayName("Visual Circuit")
@CustomTools ( CircuitToolsProvider.class )
@DefaultCreateButtons ( { CircuitComponent.class } )
//@CustomToolButtons ( { SimulationTool.class } )

public class VisualCircuit extends AbstractVisualModel {

	private Circuit circuit;

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first instanceof VisualContact && second instanceof VisualContact) {
			if (!(((Contact)((VisualContact)first).getReferencedComponent()).getIOType()==Contact.IOType.OUTPUT&&
				((Contact)((VisualContact)second).getReferencedComponent()).getIOType()==Contact.IOType.INPUT)
				)
				throw new InvalidConnectionException ("Connection is only valid for contacts of opposite IO types");
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

		VisualComponent c1 = (VisualContact) first;
		VisualComponent c2 = (VisualContact) second;
		MathConnection con = (MathConnection) circuit.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
		VisualConnection ret = new VisualConnection(con, c1, c2);
		Hierarchy.getNearestContainer(c1, c2).add(ret);

	}



}
