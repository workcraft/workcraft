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
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;

@DisplayName("Digital Circuit")
@ShortName("circuit")
@CustomTools(CircuitToolsProvider.class)
public class VisualCircuit extends AbstractVisualModel {

	private Circuit circuit;

	@Override
	public void validateConnection(Node first, Node second) throws InvalidConnectionException {
		if (first==second) {
			throw new InvalidConnectionException ("Connections are only valid between different objects");
		}

		if (second instanceof VisualConnection) {
			throw new InvalidConnectionException ("Merging connections is not allowed");
		}

		if (second instanceof VisualComponent) {
			for (Connection c: this.getConnections(second)) {
				if (c.getSecond() == second)
					throw new InvalidConnectionException ("Only one connection is allowed as a driver");
			}
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

	public VisualCircuit(Circuit model, VisualGroup root) {
		super(model, root);
		circuit = model;
	}

	public VisualCircuit(Circuit model) throws VisualModelInstantiationException {
		super(model);
		circuit = model;
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		if (first instanceof VisualConnection) {
			VisualJoint joint = new VisualJoint(new Joint());
			addJoint(joint);
			VisualConnection connection = (VisualConnection)first;
			joint.setPosition(connection.getPointOnConnection(0.5));
			remove(connection);
			connect(connection.getFirst(), joint);
			connect(joint, connection.getSecond());
			first = joint;
		}

		if ((first instanceof VisualComponent) && (second instanceof VisualComponent)) {
			VisualComponent vComponent1 = (VisualComponent)first;
			MathNode mComponent1 = vComponent1.getReferencedComponent();

			VisualComponent vComponent2 = (VisualComponent)second;
			MathNode mComponent2 = vComponent2.getReferencedComponent();

			MathConnection mConnection = (MathConnection)circuit.connect(mComponent1, mComponent2);
			VisualCircuitConnection vConnection = new VisualCircuitConnection(mConnection, vComponent1, vComponent2);

			Node vParent = Hierarchy.getCommonParent(vComponent1, vComponent2);
			VisualGroup vGroup = Hierarchy.getNearestAncestor(vParent, VisualGroup.class);
			vGroup.add(vConnection);

			Container mParent = (Container)(mConnection.getParent());
			Container mContainer = getMathContainer(this, vGroup);
			LinkedList<Node> mConnections = new LinkedList<Node>();
			mConnections.add(mConnection);
			mParent.reparent(mConnections, mContainer);
		}
	}

	public Collection<VisualFunctionContact> getVisualFunctionContacts() {
		return Hierarchy.getChildrenOfType(getRoot(), VisualFunctionContact.class);
	}

	public Collection<Environment> getEnvironments() {
		return Hierarchy.getChildrenOfType(getRoot(), Environment.class);
	}

	public VisualFunctionContact getOrCreateOutput(String name, double x, double y) {
		VisualFunctionContact vc = getOrCreateContact(getCurrentLevel(), name, IOType.OUTPUT, x, y);
		return vc;
	}

	public VisualFunctionContact  getOrCreateComponentOutput(VisualFunctionComponent component,  String name, double x, double y) {
		VisualFunctionContact vc = getOrCreateContact(component, name, IOType.OUTPUT, x, y);
        vc.setPosition(new Point2D.Double(x, y));
		return vc;
	}

	public VisualFunctionContact getOrCreateContact(Container container, String name, IOType ioType, double x, double y) {
		// here "parent" is a container of a visual model
		if (name != null) {
			for (Node n: container.getChildren()) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact contact = (VisualFunctionContact)n;
					String contactName = getMathModel().getName(contact.getReferencedContact());
					if (name.equals(contactName)) {
						return contact;
					}
				} // TODO: if found something else with that name, return null or exception?
			}
		}

		Direction direction = Direction.WEST;
		if (ioType == null) {
			ioType = IOType.OUTPUT;
		}
		if (ioType == IOType.OUTPUT) {
			direction = Direction.EAST;
		}

		VisualFunctionContact vc = new VisualFunctionContact(new FunctionContact(ioType));
		vc.setDirection(direction);
		vc.setPosition(new Point2D.Double(x, y));

		if (container instanceof VisualFunctionComponent) {
			VisualFunctionComponent component = (VisualFunctionComponent)container;
			component.addContact(this, vc);
		} else {
			Container mathContainer = AbstractVisualModel.getMathContainer(this, getRoot());
			mathContainer.add(vc.getReferencedComponent());
			add(vc);
		}
		if (name != null) {
			circuit.setName(vc.getReferencedComponent(), name);
		}
		return vc;
	}

	public void addFunctionComponent(VisualFunctionComponent component) {
		for (Node node : component.getMathReferences()) {
			circuit.add(node);
		}
		super.add(component);
	}

	 public void addJoint(VisualJoint joint) {
		 for (Node node : joint.getMathReferences()) {
			 circuit.add(node);
		 }
		 super.add(joint);
	 }

	 @NoAutoSerialisation
	public File getEnvironmentFile() {
		File result = null;
		for (Environment env: getEnvironments()) {
			result = env.getFile();
		}
		return result;
	}

	@NoAutoSerialisation
	public void setEnvironmentFile(File value) {
		for (Environment env: getEnvironments()) {
			remove(env);
		}
		Environment env = new Environment();
		env.setFile(value);
		add(env);
	}

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node == null) {
			properties.add(new EnvironmentFilePropertyDescriptor(this));
		} else if (node instanceof VisualFunctionContact) {
			VisualFunctionContact contact = (VisualFunctionContact)node;
			VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
			properties.add(props.getSetProperty(contact));
			properties.add(props.getResetProperty(contact));
		}
		return properties;
	}


}
