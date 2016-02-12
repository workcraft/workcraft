package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Identifier;

public class CircuitReferenceManager extends HierarchicalUniqueNameReferenceManager {

    public CircuitReferenceManager(NamespaceProvider provider, References refs) {
        super(refs);
    }

    @Override
    public String getName(Node node) {
        NameManager mgr = getNameManager(getNamespaceProvider(node));
        if ((node instanceof Contact) && !mgr.isNamed(node)) {
            return ((Contact) node).getName();
        }
        if ((node instanceof FunctionComponent) && !mgr.isNamed(node)) {
            return ((FunctionComponent) node).getName();
        }
        if (!mgr.isNamed(node)) {
            return null;
        }
        return super.getName(node);
    }

    @Override
    public void setName(Node node, String name) {
        // support for the older models
        if (Identifier.isNumber(name) && (node instanceof Contact)) {
            String nodeName = ((Contact) node).getName();
            if ((nodeName != null) && !nodeName.isEmpty()) {
                name = nodeName;
            }
        } else if (Identifier.isNumber(name) && (node instanceof CircuitComponent)) {
            String nodeName = ((CircuitComponent) node).getName();
            if ((nodeName != null) && !nodeName.isEmpty()) {
                name = nodeName;
            }
        } else if (Identifier.isNumber(name)) {
            name = getPrefix(node) + name;
        }
        if (node instanceof Contact) {
            // propagate info to the node itself
            ((Contact) node).setName(name);
        }
        if (node instanceof CircuitComponent) {
            // propagate info to the node itself
            ((CircuitComponent) node).setName(name);
        }
        super.setName(node, name);
    }

}
