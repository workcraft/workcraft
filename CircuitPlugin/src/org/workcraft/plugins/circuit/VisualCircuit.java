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

import java.awt.geom.Point2D;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.util.Hierarchy;


@DisplayName("Digital Circuit")
@CustomTools ( CircuitToolsProvider.class )
@DefaultCreateButtons ( { Joint.class, FunctionComponent.class } )

public class VisualCircuit extends AbstractVisualModel {

	private Circuit circuit;

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first==second) {
			throw new InvalidConnectionException ("Connections are only valid between different objects");
		}

		if (first instanceof VisualCircuitConnection || second instanceof VisualCircuitConnection) {
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
			VisualCircuitConnection connection = new VisualCircuitConnection(con, c1, c2);
			Node parent = Hierarchy.getCommonParent(c1, c2);
			VisualGroup nearestAncestor = Hierarchy.getNearestAncestor (parent, VisualGroup.class);
			nearestAncestor.add(connection);
		}
	}

	@Override
	public Properties getProperties(Node node) {
		if(node instanceof VisualFunctionContact)
		{
			VisualFunctionContact contact = (VisualFunctionContact)node;
			VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
			return Properties.Merge.add(super.getProperties(node),
					props.getSetProperty(contact),
					props.getResetProperty(contact));
		}
		else return super.getProperties(node);
	}

	public VisualFunctionContact  getOrCreateOutput(String name, double x, double y) {

		for(VisualFunctionContact c : Hierarchy.filterNodesByType(getRoot().getChildren(), VisualFunctionContact.class)) {
			if(c.getName().equals(name)) return c;
		}

		FunctionContact fc = new FunctionContact(IOType.OUTPUT);
		VisualFunctionContact vc = new VisualFunctionContact(fc);
		Point2D p2d = new Point2D.Double();
		p2d.setLocation(x,y);
		vc.setPosition(p2d);
		circuit.add(fc);
		this.add(vc);

		vc.setName(name);

		return vc;
	}

}
