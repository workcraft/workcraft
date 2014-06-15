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

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;


@DisplayName("Digital Circuit")
@CustomTools ( CircuitToolsProvider.class )
public class VisualCircuit extends AbstractVisualModel {

	private Circuit circuit;

	@Override
	public void validateConnection(Node first, Node second) throws InvalidConnectionException {
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

	public VisualCircuit(Circuit model, VisualGroup root) {
		super(model, root);
		circuit=model;
	}

	public VisualCircuit(Circuit model) throws VisualModelInstantiationException {
		super(model);
		circuit=model;
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
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

	public VisualFunctionContact  getOrCreateOutput(String name, double x, double y) {

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

		if (name!=null) {

			for (Node n: container.getChildren()) {
				if (n instanceof VisualFunctionContact) {
					if (getMathModel().getName(((VisualFunctionContact)n).getReferencedContact()).equals(name))
						return (VisualFunctionContact)n;
				} // TODO: if found something else with that name, return null?
			}

		}

		// the name is available
		// create a new contact if it was not found
		VisualContact.Direction dir=null;
		if (ioType==null) ioType = IOType.OUTPUT;
		dir=VisualContact.Direction.WEST;
		if (ioType==IOType.OUTPUT)
			dir=VisualContact.Direction.EAST;




		VisualFunctionContact vc = new VisualFunctionContact(new FunctionContact(ioType));
		vc.setDirection(dir);
		vc.setPosition(new Point2D.Double(x, y));

		if (container instanceof VisualFunctionComponent) {
			VisualFunctionComponent component = (VisualFunctionComponent)container;

			component.addContact(this, vc);

			if (name!=null)
				circuit.setName(vc.getReferencedComponent(), name);


			vc.setSetFunction(One.instance());
			vc.setResetFunction(One.instance());
		} else {

	        AbstractVisualModel.getMathContainer(this, getRoot()).add(vc.getReferencedComponent());
			add(vc);

			if (name!=null)
				circuit.setName(vc.getReferencedComponent(), name);

		}

		return vc;
	}

//	public VisualFunctionContact addFunction(VisualCircuitComponent component, IOType ioType) {
//		VisualContact.Direction dir=null;
//		if (ioType==null) ioType = IOType.OUTPUT;
//		dir=VisualContact.Direction.WEST;
//
//		if (ioType==IOType.OUTPUT) {
//			dir=VisualContact.Direction.EAST;
//		}
//
//		FunctionContact c = new FunctionContact(ioType);
//		VisualFunctionContact vc = new VisualFunctionContact(c);
//		vc.setDirection(dir);
//
//		//circuit.setName(component.getReferencedComponent(), Contact.getNewName(component.getReferencedComponent(), prefix, null, allowShort));
//
//		component.addContact(vc);
//		return vc;
//	}


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

//	 public void addFunctionContact(VisualFunctionComponent component, VisualFunctionContact contact) {
//		 component.add(contact);
//		 component.getReferencedCircuitComponent().add(contact.getReferencedContact());
//	 }

	 @NoAutoSerialisation
	public File getEnvironmentFile() {
		File result = null;
		for (Environment env: Hierarchy.filterNodesByType(getRoot().getChildren(), Environment.class)) {
			result = env.getFile();
		}
		return result;
	}

	@NoAutoSerialisation
	public void setEnvironmentFile(File value) {
		for (Environment env: Hierarchy.filterNodesByType(getRoot().getChildren(), Environment.class)) {
			remove(env);
		}
		Environment env = new Environment();
		env.setFile(value);
		add(env);
	}

	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);
		if (node == null) {
			properties = Properties.Merge.add(properties,
					new EnvironmentFilePropertyDescriptor(this));
		} else if (node instanceof VisualFunctionContact) {
			VisualFunctionContact contact = (VisualFunctionContact)node;
			VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
			properties = Properties.Merge.add(properties,
					props.getSetProperty(contact),
					props.getResetProperty(contact));
		}
		return properties;
	}


}
