package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Identifier;


public class CircuitReferenceManager extends HierarchicalUniqueNameReferenceManager {

	public CircuitReferenceManager(NamespaceProvider provider,
			References existing, Func<Node, String> defaultName) {
		super(existing, defaultName);
	}

	@Override
	public String getName(Node node) {

		NameManager<Node> man = getNameManager(getNamespaceProvider(node));

		if (node instanceof Contact && !man.isNamed(node)) {
			return ((Contact)node).getName();
		}

		if (node instanceof FunctionComponent && (!man.isNamed(node))) {
			return ((FunctionComponent)node).getName();
		}

		if (!man.isNamed(node)) return null;

		return super.getName(node);
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
			// name="_"+name;
			name=defaultName.eval(node)+name;
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
