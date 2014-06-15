package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Identifier;


public class CircuitReferenceManager extends HierarchicalUniqueNameReferenceManager {

	public CircuitReferenceManager(NamespaceProvider provider,
			References existing, Func<Node, String> defaultName) {
		super(provider, existing, defaultName);
	}


	@Override
	public void setName(Node node, String name) {

		// support for the older models
		if (Identifier.isNumber(name) && node instanceof Contact) {

			String n = ((Contact)node).getName();
			if (n!=null&&!n.equals("")) name=n;
		} else if (Identifier.isNumber(name) && node instanceof CircuitComponent) {

			String n = ((CircuitComponent)node).getName();
			if (n!=null&&!n.equals("")) name=n;

		} else if (Identifier.isNumber(name)) {

			name="_"+name;
		}

		if (node instanceof Contact) {
			// propagate info to the node itself
			((Contact)node).setName(name);
		}
		if (node instanceof CircuitComponent) {
			// propagate info to the node itself
			((CircuitComponent)node).setName(name);
		}

		super.setName(node, name);
	}

}
